# JackCheques
A Minecraft 1.14 Spigot plugin that adds Cheques and Vouchers.

Dependencies: 
 - Essentials
 - Vault

## Commands

| Command  | Description |
|---|---|
| /jackcheques | Base command for managing the plugin |
| /jackcheques ? | Shows the help |
| /jackcheques help | Shows the help |
| /jackcheques version | Shows version information and plugin description. Needs the permission node **jackcheques.admin** |
| /jackcheques debug | Toggles debug mode. Needs the permission node **jackcheques.admin** |
| /jackcheques reload | Reloads config and message file. Needs the permission node **jackcheques.admin** |
| /jackcheques save | Save config and message file to disk. Needs the permission node **jackcheques.admin** | 
| /withdraw ? | Shows the help. Needs the permission node **jackcheques.withdraw**|
| /withdraw &lt;amount&gt; | Create Cheque with given amount. Needs the permission node **jackcheques.withdraw** |
| /withdraw &lt;amount&gt; &lt;description&gt; | Create Cheque with given amount and description text. Needs the permission node **jackcheques.withdraw** and **jackcheques.withdraw.description** |
| /voucher ? | Shows the help. Needs the permission node **jackcheques.voucher**|
| /voucher &lt;player&gt; &lt;amount&gt; | Create voucher for given player with given amount. This will put the voucher in the inventory of the player if there is space. Needs the permission node **jackcheques.voucher** |
| /voucher &lt;player&gt; &lt;amount&gt; &lt;description&gt; | Create Cheque with given amount and description text. This will put the voucher in the inventory of the player if there is space. Needs the permission node **jackcheques.voucher** and **jackcheques.voucher.description** |

## Permission nodes

| Permissions | User group |
|---|---|
| jackcheques.admin | admin |
| jackcheques.voucher | admin |
| jackcheques.voucher.description | admin | 
| jackcheques.withdraw | user |
| jackcheques.withdraw.description | user | 

## Config
```yaml
# Don't touch pls
ConfigVersion: "${project.version}"

# Debug options, don't touch unless needed
#
# DebugLevel: 0 = no debug, 1 = debug, 2 = debug with stack traces
#
DebugLevel: 0
ForcedLanguage: EN


# Note: The withdraw/deposit messages are from Essentials
# Currency prefix
CurrencyPrefix: ""

# Currency suffix
CurrencySuffix: " Coins"

MinimumChequeAmount: 1
```

