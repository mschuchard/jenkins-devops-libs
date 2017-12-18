package devops.common;

// checks input value for default value use if not set
@NonCPS
def default_input(input, default_value) {
  return input == null ? default_value : input
}
