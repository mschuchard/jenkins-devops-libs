### 2.0.1 (Next)
**General**
- Improve `sh` step labels for iterative invocations.
- Fix syntax error in `makeDirParents` library method.
- Check if directory exists on build agent in `makeDirParents`.

**Helm**
- Validate YAML file parameter values.

**Packer**
- Change into directory when `template` parameter is directory of templates/configs.

### 2.0.0
**General**
- Global variable methods and associated parameters converted to camelCase.
- Remove support for old DSL/closure type inputs to global variable methods.
- Remove global variable methods for software installation and configuration.

**Helm**
- Add `dryRun` parameter to `install` and `upgrade` methods.
- Add `show` method.

**Terraform**
- Add `out` and `replace` parameters to `plan` method.
- Fix `json` parameter for `validate` method.
- Add `return` parameter to `validate` method.
- Fix `resources` parameter for `imports` method.
- Fix return type for `validate` method.

### 1.6.2
**General**
- Replace `File.mkdir()` class method with `dir` step method (valid also on agents).
- Add `makeDirParents` method to attempt to fix missing user directories.
- Reorganize try/catch blocks to be more precise.

**AWX**
- Finish initial implementation and promote to beta.

**Packer**
- Add `init` method.
- Fix `var_file` parameter in applicable methods.

**Puppet**
- Fix `credentials_id` usage.
- Update `token` param to `tokenFile`.
- Fix syntax errors.

**Terraform**
- Change working directory to config directory before command execution.

### 1.6.1
**General**
- Deprecate software `install` methods.
- Fine tune nested type specifications.

**AWX**
- Initialize as alpha.

**Helm**
- Add `namespace` parameter to `test` method.

**Puppet**
- Enable `withCredentials` bindings for PE token for methods.

**Terraform**
- List workspace information when selection fails.
- Add `list` option to `command` parameter for `state` method.
- Add `refreshOnly` parameter to `plan` method.
- Add `display` parameter to `output` method.
- Add `backendConfig` parameter to `init` method.
- Fix `var_file` parameter in all relevant methods.
- Ignore useless parameters for `apply` and `destroy` methods when `config_path` value is a plan file.

### 1.6.0
**Helm**
- Drop support for versions < 3.0.
- Add `plugin` and `status` methods.
- Add `namespace` parameter to `uninstall` and `rollback` methods.

**Packer**
- Drop support for versions < 1.5.
- Add `fmt` method.
- Add `force` and `on_error` parameters to `build` method.

**Terraform**
- Drop support for versions < 0.12.
- Update validate method for >= 0.12 only.
- Add `output` method.
- Prevent `fmt` parameter incompatibility with `check` and `write`.
- Rename `state` method `cmd` parameter to `command`.

**Puppet**
- Convert REST API requests to utilize `http_request` plugin.

### 1.5.0
**GoSS**
- Convert `flags` parameter in `validate_docker` from `list<string>` to `map` type.

**Helm**
- Add `uninstall` method alias.
- Add `repo` method.
- Prevent initialization for versions >= 3.
- Require `name` parameter for `install` method.
- Convert `set` parameter in `install`, `lint`, and `upgrade` methods from `list<string>` to `map` type.

**OpenFaaS**
- Convert `query` and `header` parameters in `invoke`, and `label` in `deploy` from `list<string>` to `map` type.

**Packer**
- Convert `var` parameters from `list<string>` to `map` type.

**Terraform**
- Add `backend` parameter to `init` method.
- Convert `var` parameters from `list<string>` to `map` type.
- Convert `resources` parameter for `move` value for `command` parameter in `state` method from `list<list<string>>` to `map` type.
- Fix `state` method incorrect `sh` step method prefix.

### 1.4.1
**General**
- Fine tune type specifications, especially in global var method returns.

**GoSS**
- Fix `serve` port specification.

**Helm**
- Add `install` parameter to `upgrade` method.

**Terraform**
- Fix syntax error in `fmt` method.

### 1.4.0
**General**
- Support new and old Pipeline DSL.
- Add labels to shell methods for clarity.

**Helm**
- Enable native logging for `test` method if available.

**Terraform**
- Update `taint` method for new 0.12 usage.
- Add `return` parameter to `plan` method.
- Promote `fmt` method to supported.

### 1.3.1
**General**
- Add type specification checks.
- Fixed explicit List type check on params.

**GoSS**
- Fix port default value in `server` method.

**Helm**
- Rename `package` method to `packages` to avoid reserved name collision.

**Packer**
- Fix bugs in methods.

**Terraform**
- Add `fmt` method as beta.
- Add `target` and `display` parameters to `plan` method.
- Rename `import` method to `imports` to avoid reserved name collision.
- Fix bugs in methods.

### 1.3.0
**Helm**
- Allow values override to also be a URL.
- Add `keyring` param to `package` method.
- Add `kubectl` method.

**Packer**
- Add `inspect` method.
- Change `only` parameter input type.

**Puppet**
- Add `token` method.

**Terraform**
- Add `taint`, `state`, and `import` methods.
- Add `check_vars` parameter to `validate` method.
- Add `destroy` parameter to `plan` method.
- Handle 0.12 changes to `validate` method.
- Checks for required parameters to methods.
- Change `dir` to `config_path` param for `destroy` method, and also allow for plan file arguments.

### 1.2.1
**General**
- Fix `mapToJSON` common method and update per documented example.
- Various fixes, cleanup, and optimization.

**GoSS**
- Fix flag setting and usage in `validate_docker`.

**OpenFaaS**
- Add necessary `function` and optional `tls` params to `invoke` method.

### 1.2.0
**Helm**
- Add chart provenance verification to applicable methods.
- Add `lint`, `package`, and `test` methods.
- `values` param is now an array of strings.

**OpenFaaS**
- Added `invoke`, `login`, `push`, and `remove` methods.
- Fixed `deploy` and `build` invalid usage issue.

**Puppet**
- Fix `scope` param for `.task`.

**Terraform**
- Changed `init` usage to block DSL and added `plugin_dir` and `upgrade` params.
- Modified `plugin_install` usage to block DSL and added `install_loc` param.
- `env.TF_IN_AUTOMATION` added to methods.

### 1.1.0
**Helm**
- Added `context` param to applicable methods.
- Enabled multiple `set` parameter values for applicable methods.
- Changed `delete` usage to block DSL.

**Packer**
- Added `plugin_install` method.

**Puppet**
- Added `code_deploy` and `task` methods.

**Terraform**
- Changed `apply`, `destroy`, `plan`, and `validate` usage to block DSL.
- Added `var_file`, `var`, and `target` params to applicable methods.
- Added `plugin_install` method.

### 1.0.0
Initial release.
