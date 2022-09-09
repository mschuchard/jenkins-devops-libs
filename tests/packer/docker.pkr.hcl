source "docker" "example" {
  image = "centos:7"
}

build {
  sources = ["source.docker.example"]
}
