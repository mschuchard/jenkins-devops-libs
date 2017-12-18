package devops.common;

// checks input value for default value use if not set
def default_input(input, default_value) {
  input == null ? default_value : input
}

// validates yaml syntax
def yaml_validate(yaml_file) {
  // stuff
}
