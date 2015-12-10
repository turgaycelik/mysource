package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.jira.webtests.cargo.CargoTestHarness;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;

/**
 * Proxy runner to create cargo-enabled runner.
 *
 * @since v4.4
 */
public class CargoSuiteRunner extends Runner
{
    private final Runner cargoRunner;

    public CargoSuiteRunner(Class<?> webTestSuiteClass) throws InitializationError
    {
        try
        {
            this.cargoRunner = CargoTestHarness.cargoRunner(new EnvironmentAwareSuiteRunner(webTestSuiteClass));
        }
        catch (IOException e)
        {
            throw new InitializationError(e);
        }
    }

    @Override
    public Description getDescription()
    {
        return cargoRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier)
    {
        cargoRunner.run(notifier);
    }
}
