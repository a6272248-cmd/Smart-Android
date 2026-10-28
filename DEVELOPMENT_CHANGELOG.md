# Smart-Android development snapshot

Branch: `codex/development-audit`
HEAD: `28e4242`

Commits included:
- `4a8b7a2` Harden manifest cleartext traffic policy
- `d703db2` Add Android Keystore encrypted connection storage
- `017b7b9` Use Keystore storage for router password
- `08d9488` Reconcile voucher serials with router state

The snapshot does not contain credentials, signing keys, or router data.

## 0.4.0-preview network management
- Added encrypted multi-router profiles with active profile selection.
- Added read-only monitoring and reports for RouterOS resource and interface metrics.
- Added tabbed native network views for DHCP, DNS, queues, and firewall resources.
- Emulator UI workflow remains optional; Java/Mock and Gradle validation remain required.
