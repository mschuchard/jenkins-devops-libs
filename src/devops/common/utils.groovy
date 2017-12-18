package devops.common;

// checks input value for default value use if not set
def default_input(input, default_value) {
  return input == null ? default_value : input
}
// import devops.common.utils
// def utils = new utils()
// utils.default_input(foo, 'bar')
