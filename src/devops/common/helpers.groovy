// src/devops/common/helpers.groovy
package devops.common

// private method for vars
List<String> varSubCmd(Map config) {
    List<String> subCmd = []

    // check for optional var inputs
    if (config.varFile) {
        assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

        subCmd.add("-var-file=${config.varFile}")
    }
    if (config.var) {
        assert (config.var in Map) : 'The var parameter must be a Map.'

        config.var.each { String var, String value ->
            // convert value to json if not string type
            if (value in List || value in Map) {
                value = writeJSON(json: value, returnText: true)
            }

            subCmd.addAll(['-var', "${var}=${value}"])
        }
    }

    return subCmd
}
