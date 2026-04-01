# Approach A: Keep --link-at-build-time

## Strategy
Keep `--link-at-build-time` in the app layer and handle missing optional dependencies
using native-image flags (`--initialize-at-run-time`, `--exclude-config`, etc.).

## Iteration Log

### Iteration 1
- Starting state: `--link-at-build-time` added back to the native-image invocation
- Expected error: `NoClassDefFoundError: io/netty/internal/tcnative/SSLPrivateKeyMethod`
- Running build...
