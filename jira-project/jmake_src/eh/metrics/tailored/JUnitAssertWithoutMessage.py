import re
from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger

class JunitAssertWithoutMessage(MetricsCollector):

    def __init__(self, metrics_name: str, description: str = None, metrics_logger: MetricsLogger = None):
        super().__init__(metrics_name, description, metrics_logger)

        self.scanner=re.Scanner([
            (r"import",  	                lambda scanner,token:"IMPORT"),
            (r"(Assert\.)?(assertTrue|assertFalse)",  	lambda scanner,token:"ASSERT_START"),
            (r"\(",    	                    lambda scanner,token:"OPEN_BRACE"),
            (r"\)",    	                    lambda scanner,token:"CLOSE_BRACE"),
            (r",",    	                    lambda scanner,token:"ARG_SEPARATOR"),
            (r"\s+", None),
            (r"[^\(\),]+",    	            lambda scanner,token:"OTHER"),
        ])

    def wants_file(self, file_name: str):
        self.file_name = file_name
        return file_name.endswith('.java')

    def on_read_line(self, line: str):
        # Guard to prevent scanning line uneccessarily
        if "assertTrue" in line or "assertFalse" in line:
            tokens, remainder = self.scanner.scan(line)

            state = "start"

            brace_count = 0

            for token in tokens:
                if state == "start" and token == "IMPORT":
                    # Found an import statement at the start of line, let's go
                    return True
                elif state == "start" and token == "ASSERT_START":
                    state = "found_assert"
                elif state == "found_assert" and token == "OPEN_BRACE":
                    brace_count += 1
                elif state == "found_assert" and token == "CLOSE_BRACE":
                    brace_count -= 1
                elif state == "found_assert" and brace_count == 1 and token == "ARG_SEPARATOR":
                    # We've found our argument separator, we're good here.
                    return True

            # Got to the end of the line without finding the arg separator, we're in trouble.
            self.hit("assert without message found: %s in file: %s" % (line.strip(), self.file_name))
        return True