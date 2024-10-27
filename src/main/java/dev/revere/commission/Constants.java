package dev.revere.commission;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
public final class Constants {
    private Constants() {}

    public static final String PROJECT_NAME = "Tonic Consulting";
    public static final String PROJECT_DESCRIPTION = "A Discord bot that manages commissions for freelancers.";
    public static final String COMPANY_NAME = "Revere Development";

    public static final long MAIN_GUILD_ID = 1281733553138303016L;
    public static final long COMMISSION_GUILD_ID = 1299043117474910229L;

    public static final long WELCOME_CHANNEL_ID = 1299489838533447701L;
    public static final long TRANSCRIPT_CHANNEL_ID = 1299515030760591431L;
    public static final long FREELANCING_BASICS_CHANNEL_ID = 1300092357697343599L;

    public static final long GLOBAL_FREELANCER_ROLE_ID = 1282432859881345047L;

    public static final String TITLE_LOGIN = "Login | " + PROJECT_NAME;
    public static final String TITLE_LOGOUT = "Logout | " + PROJECT_NAME;
    public static final String TITLE_ACCOUNTS = "Accounts | " + PROJECT_NAME;
    public static final String TITLE_DASHBOARD = "Dashboard | " + PROJECT_NAME;
    public static final String TITLE_COMMISSIONS = "Commissions | " + PROJECT_NAME;
    public static final String TITLE_FREELANCERS = "Freelancers | " + PROJECT_NAME;

    public static final String PATH_LOGIN = "login";
    public static final String PATH_LOGOUT = "logout";
    public static final String PATH_ACCOUNTS = "accounts";
    public static final String PATH_DASHBOARD = "dashboard";
    public static final String PATH_COMMISSIONS = "commissions";
    public static final String PATH_FREELANCERS = "freelancers";


}
