package devops.common;

// checks input value for default value use if not set
def default_input(input, default_value) {
  return input == null ? default_value : input
}

// removes file
def remove_file(String file) {
  new File(file).delete()
}

// downloads file
def download_file(String url, String dest) {
  def file = new File(dest).newOutputStream()
  file << new URL(url).openStream()
  file.close()
  // file = null // should not need this line since this method is CPS anyway?
}

//http://vertx.io/docs/groovydoc/io/vertx/groovy/core/file/FileSystem.html
//@Whitelisted
