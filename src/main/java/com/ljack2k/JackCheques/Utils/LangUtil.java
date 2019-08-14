/*
#
#   This file has been inspired by the guys at DiscordSRV. They make a very awesome
#   Discord plugin for Spigot that I recommend to everybody. Since I'm a beginner
#   coder I learned a lot from their code. Copied a lot of the structure of DiscordSRV
#   because it is so damm good.
#
#   You should totaly check them out at https://www.spigotmc.org/resources/discordsrv.18494/
#   or their github https://github.com/Scarsz/DiscordSRV
#
*/

package com.ljack2k.JackCheques.Utils;

import com.ljack2k.JackCheques.JackCheques;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LangUtil {

    public enum Language {

        EN("English");

        @Getter final String code;
        @Getter final String name;

        Language(String name) {
            this.code = name().toLowerCase();
            this.name = name;
        }

    }

    /**
     * Fixed messages
     */
    @SuppressWarnings("serial")
    public enum InternalMessage {
		CANNOT_LOAD_CONFIG_WARNING(new HashMap<Language, String>() {{
            put(Language.EN, "Configuration file cannot be loaded.");
        }}), CANNOT_LOAD_LANG_FILE_WARNING(new HashMap<Language, String>() {{
            put(Language.EN, "Language file cannot be opened");
        }}), LANGUAGE_INITIALIZED(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Language file loaded");
        }}), INVALID_CONFIG(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Invalid Config files");
        }}), ERROR_DATABASE_SETUP(new HashMap<Language, String>() {{
        	put(Language.EN, "Error setting up database!");
        }}), CONFIG_RELOAD(new HashMap<Language, String>() {{ 
        	put(Language.EN, "JackCheques Config Reloaded.");
        }}), CONFIG_SAVED(new HashMap<Language, String>() {{ 
        	put(Language.EN, "Config file saved");
        }});
    	
    	
    	
    	
    	
        @Getter private final Map<Language, String> definitions;
        InternalMessage(Map<Language, String> definitions) {
            this.definitions = definitions;

            // warn about if a definition is missing any translations for messages
            for (Language language : Language.values())
                if (!definitions.containsKey(language))
                    JackCheques.debug("Language " + language.getName() + " missing from definitions for " + name());
        }

        @Override
        public String toString() {
            return definitions.getOrDefault(userLanguage, definitions.get(Language.EN));
        }

    }

    public enum Message {

    	NOT_INGAME_PLAYER("NotIngamePlayer", true),
    	NO_COMMAND_PERMISSION("NoCommandPermission", true),
        HELP_COMMAND_HEADER("HelpCommandHeader", true),
    	HELP_COMMAND("HelpCommand", true),
    	HELP_EMPTY_COMMAND("HelpEmptyCommand", true),
    	HELP_RELOAD_COMMAND("HelpReloadCommand", true),
    	HELP_SAVE_COMMAND("HelpSaveCommand", true),
    	HELP_DEBUG_COMMAND("HelpDebugCommand", true),
    	HELP_VERSION_COMMAND("HelpVersionCommand", true),
    	CURRENT_VERSION("CurrentVersion", true),
    	CHATHEADER_VERSION_INFORMATION("ChatHeaderVersionInformation", true),
        HELP_WITHDRAW("HelpWitdrawCommand", true),
        SOMETHING_WRONG("SomethingWrong", true),
        NEGATIVE_AMOUNT("NegativeAmount", true),
    	NO_AMOUNT("NoAmountGiven", true),
        YOUR_INVENTORY_FULL("YourInventoryFull", true),
        PLAYER_INVENTORY_FULL("PlayerInventoryFull", true),
        NO_PERMISSION_DESCRIPTION("NoPermissionDescription", true),
        VOUCHER_AMOUNT_ERROR("VoucherAmountError", true),
        CHEQUE_AMOUNT_ERROR("ChequeAmountError", true),
        VOUCHER_NO_PLAYER_GIVEN("VoucherNoPlayerGiven", true),
        VOUCHER_GIVEN("VoucherGiven", true),
        VOUCHER_RECEIVED("VoucherReceived", true),
        CHEQUE_CREATED("ChequeCreated", true),
        CHEQUE("Cheque", true),
        VOUCHER("Voucher", true),
        AMOUNT("Amount", true),
        SIGNED_BY("SignedBy", true),
        ISSUED_TO("IssuedTo", true),
        DESCRIPTION("Description", true),
        USE_CHEQUE("UseCheque", true),
        USE_VOUCHER("UseVoucher", true),
        HELP_VOUCHER_HEADER("HelpVoucherHeader", true),
        HELP_WITHDRAW_HEADER("HelpWithdrawHeader", true),
        HELP_VOUCHER_DESCRIPTION_NO_PERMISSION("HelpVoucherDescriptionNoPermission", true),
        HELP_VOUCHER_DESCRIPTION_HAS_PERMISSION("HelpVoucherDescriptionHasPermission", true),
        HELP_CHEQUE_DESCRIPTION_NO_PERMISSION("HelpChequeDescriptionNoPermission", true),
        HELP_CHEQUE_DESCRIPTION_HAS_PERMISSION("HelpChequeDescriptionHasPermission", true),
        NOT_ENOUGH_MONEY("NotEnoughMoney", true),
        VOUCHER_CANT_FIND_PLAYER("VoucherCantFindPlayer", true)
        ;

        @Getter private final String keyName;
        @Getter private final boolean translateColors;

        Message(String keyName, boolean translateColors) {
            this.keyName = keyName;
            this.translateColors = translateColors;
        }

        @Override
        public String toString() {
            String message = messages.getOrDefault(this, "");
            return message;
        }

    }

    @Getter private static final Map<Message, String> messages = new HashMap<>();
    @Getter private static final Yaml yaml = new Yaml();
    @Getter private static Language userLanguage;
    static {
        String languageCode = System.getProperty("user.language").toUpperCase();

        String forcedLanguage = JackCheques.config().getString("ForcedLanguage");
        if (StringUtils.isNotBlank(forcedLanguage) && !forcedLanguage.equalsIgnoreCase("none")) {
            if (forcedLanguage.length() == 2) {
                languageCode = forcedLanguage.toUpperCase();
            } else {
                for (Language language : Language.values()) {
                    if (language.getName().equalsIgnoreCase(forcedLanguage)) {
                        languageCode = language.getCode();
                    }
                }
            }
        }

        try {
            userLanguage = Language.valueOf(languageCode);
        } catch (Exception e) {
            userLanguage = Language.EN;

            JackCheques.info("Unknown user language " + languageCode.toUpperCase() + ".");
            //JackCheques.info("If you fluently speak " + languageCode.toUpperCase() + " as well as English, see the GitHub repo to translate it!");
        }

        saveConfig();
        saveMessages();
        reloadMessages();

        JackCheques.info(InternalMessage.LANGUAGE_INITIALIZED + userLanguage.getName());
    }

    private static void saveResource(String resource, File destination, boolean overwrite) {
        if (destination.exists() && !overwrite) return;

        try {
            FileUtils.copyInputStreamToFile(JackCheques.class.getResourceAsStream(resource), destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        saveConfig(false);
    }
    public static void saveConfig(boolean overwrite) {
        File destination = JackCheques.getPlugin().getConfigFile();
        String resource = "/config/" + userLanguage.getCode() + ".yml";

        saveResource(resource, destination, overwrite);
    }

    public static void saveMessages() {
        saveMessages(false);
    }
    public static void saveMessages(boolean overwrite) {
        String resource = "/messages/" + userLanguage.getCode() + ".yml";
        File destination = JackCheques.getPlugin().getMessagesFile();

        saveResource(resource, destination, overwrite);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void reloadMessages() {
        if (!JackCheques.getPlugin().getMessagesFile().exists()) return;

        try {
            for (Map.Entry entry : (Set<Map.Entry>) yaml.loadAs(FileUtils.readFileToString(JackCheques.getPlugin().getMessagesFile(), Charset.forName("UTF-8")), Map.class).entrySet()) {
                for (Message message : Message.values()) {
                    if (message.getKeyName().equalsIgnoreCase((String) entry.getKey())) {
                        messages.put(message, (String) entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            JackCheques.error("Failed loading " + JackCheques.getPlugin().getMessagesFile().getPath() + ": " + e.getMessage());

            File movedToFile = new File(JackCheques.getPlugin().getMessagesFile().getParent(), "messages-" + JackCheques.getPlugin().getRandom().nextInt(100) + ".yml");
            try { FileUtils.moveFile(JackCheques.getPlugin().getMessagesFile(), movedToFile); } catch (IOException ignored) {}
            saveMessages();
            JackCheques.error("A new messages.yml has been created and the erroneous one has been moved to " + movedToFile.getPath());
            reloadMessages();
        }
    }

}