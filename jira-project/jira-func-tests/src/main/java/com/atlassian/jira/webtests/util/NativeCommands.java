package com.atlassian.jira.webtests.util;

import com.atlassian.jira.util.Function;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class NativeCommands
{
    private final static Logger log = Logger.getLogger(NativeCommands.class);

    private static final int OK = 0;
    private static final int NO_PROCESSES = 1;
    private static final int SYNTAX_ERROR = 2;
    private static final int FATAL_ERROR = 3;

    private static final String FIND_JIRA_REGEX = "java\\s.*\\s-Djira\\.dump=true";
    private static final Pattern FIND_JAVA_PATTERN = Pattern.compile("java\\s", Pattern.CASE_INSENSITIVE);

    private NativeCommands() {}

    public static void dumpTomcatThreads()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            log.info("Unable to take thread dump. Only works on Unix.");
            return;
        }

        outputJavaProcesses(false);

        SimpleProcess process = new SimpleProcess("pkill", "-3");
        String user = getUser();
        if (user != null)
        {
            process.addArguments("-U", user);
        }
        process.addArguments("-f", FIND_JIRA_REGEX);
        CommandResult<String> result = process.run();
        if (result != null)
        {
            switch (result.returnCode)
            {
                case OK:
                    break;
                case NO_PROCESSES:
                    log.info("Tried to dump JIRA threads but no VMs appeared to exist.");
                    break;
                case SYNTAX_ERROR:
                    log.error("Syntax error in pkill command line '" + process.getCommandString() + "'.");
                    break;
                case FATAL_ERROR:
                default:
                    log.error("pkill failed!");
                    break;
            }
            String output = StringUtils.stripToNull(result.output);
            if (output != null)
            {
                log.info("pkill output: " + result.output);
            }
        }
    }

    public static void outputProcessTree(boolean currentUserOnly)
    {
        if (!SystemUtils.IS_OS_MAC_OSX && !SystemUtils.IS_OS_LINUX)
        {
            log.info("Unable to generate process tree.");
            return;
        }

        String username = getUser();
        SimpleProcess process = new SimpleProcess("pstree");
        if (SystemUtils.IS_OS_MAC_OSX)
        {
            process.addArguments("-w", "-g", "0");
            if (username != null && currentUserOnly)
            {
                process.addArguments("-u", username);
            }
        }
        else
        {
            process.addArguments("-Aulap");
            if (username != null && currentUserOnly)
            {
                process.addArguments(username);
            }
        }

        final CommandResult<List<String>> r = process.runLines();
        if (r != null)
        {
            if (r.returnCode == OK)
            {
                for (String line : r.output)
                {
                    log.info(line);
                }
            }
            else
            {
                log.error("Unable to print the process tree. Command exited with '" + r.returnCode + "'");
            }
        }
    }

    public static void outputJavaProcesses(boolean currentUserOnly)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            log.info("Unable to list java processes. Only works on Unix.");
            return;
        }

        SimpleProcess process = new SimpleProcess("ps", "-eo", "pid,ppid,user,command");

        if (currentUserOnly)
        {
            String user = getUser();
            if (user != null)
            {
                process.addArguments("-u", user);
            }
        }
        
        CommandResult<List<String>> result = process.runLines();
        if (result != null)
        {
            if (result.returnCode == OK)
            {
                if (result.output.isEmpty())
                {
                    log.info("No currently running java processes are running.");
                }
                else
                {
                    log.info("The following java processes are currently running on the system: ");
                    for (String line : result.output.subList(1, result.output.size()))
                    {
                        if (FIND_JAVA_PATTERN.matcher(line).find())
                        {
                            log.info("\t" + line);
                        }
                    }
                }
            }
            else
            {
                log.error(String.format("An error occured while trying to list Java processes (%d):", result.returnCode));
                for (String line : result.output)
                {
                    log.error("\t" + line);
                }
            }
        }
    }

    private static String getUser()
    {
        return StringUtils.stripToNull(System.getProperty("user.name"));
    }

    private static class CommandResult<T>
    {
        private final int returnCode;
        private final T output;

        private CommandResult(int returnCode, T output)
        {
            this.returnCode = returnCode;
            this.output = output;
        }
    }

    private static class SimpleProcess
    {
        private static final ExecutorService workers = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Simple-Process-%d").build());

        private final List<String> commandLine = new LinkedList<String>();

        public SimpleProcess(String commandLine, String... arguments)
        {
            this.commandLine.add(commandLine);
            this.addArguments(arguments);
        }

        public SimpleProcess addArguments(String... arguments)
        {
            this.commandLine.addAll(Arrays.asList(arguments));
            return this;
        }

        public CommandResult<List<String>> runLines()
        {
            return runProcess(new Function<InputStream, List<String>>()
            {
                @Override
                public List<String> get(InputStream input)
                {
                    try
                    {
                        return IOUtils.readLines(input);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        public CommandResult<String> run()
        {
            return runProcess(new Function<InputStream, String>()
            {
                @Override
                public String get(InputStream input)
                {
                    try
                    {
                        return IOUtils.toString(input);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        private <T> CommandResult<T> runProcess(final Function<InputStream, T> converter)
        {
            ProcessBuilder builder = new ProcessBuilder().redirectErrorStream(true).command(commandLine);
            final Process process;
            try
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Running command '" + getCommandString() + "' in environment:");
                    printEnv(builder.environment(), Level.DEBUG);
                }
                process = builder.start();
            }
            catch (IOException e)
            {
                log.error("Unable to start command '" + getCommandString() + "'.", e);
                if (!log.isDebugEnabled())
                {
                    log.error("Command environment:");
                    printEnv(builder.environment(), Level.ERROR);
                }
                return null;
            }

            final InputStream inputStream = process.getInputStream();
            final OutputStream outputStream = process.getOutputStream();
            try
            {
                final Future<T> future = workers.submit(new Callable<T>()
                {
                    @Override
                    public T call()
                    {
                        return converter.get(inputStream);
                    }
                });
                int result = process.waitFor();
                if (log.isDebugEnabled())
                {
                    log.debug("Command '" + getCommandString() + "' exited with response " + result + ".");
                }
                return new CommandResult<T>(result, future.get());
            }
            catch (InterruptedException e)
            {
                log.error("Interrupted while waiting for command '" + getCommandString() + "' to finish.", e);
                process.destroy();
            }
            catch (ExecutionException e)
            {
                Throwable realEx = e;
                if (realEx.getCause() != null)
                {
                    realEx = realEx.getCause();
                    if (realEx.getCause() != null)
                    {
                        realEx = realEx.getCause();
                    }
                }
                log.error("Error while processing output for command '" + getCommandString() + "' to finish.", realEx);
                process.destroy();
            }
            finally
            {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
            return null;
        }

        public String getCommandString()
        {
            return StringUtils.join(commandLine, " ");
        }

        private void printEnv(final Map<String, String> environment, final Level level)
        {
            for (Map.Entry<String, String> entry : environment.entrySet())
            {
                log.log(level, format("\t%s = %s.", entry.getKey(), entry.getValue()));
            }
        }
    }

    public static void main(String[] args)
    {
        outputProcessTree(false);
    }
}
