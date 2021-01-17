source "docker" "example" {
  image  = "centos:7"
  commit = true
}

build {
  name    = "docker"
  sources = ["source.docker.example"]
}
