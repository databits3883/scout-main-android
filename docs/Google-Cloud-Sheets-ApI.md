# If you would like to host your own Google Cloud Project for Google Drive/Sheets API instead of utilizing the built-In one, follow these directions:

## Enable the API
Before using Google APIs, you need to turn them on in a Google Cloud project. You can turn on one or more APIs in a single Google Cloud project. [Link to enable the Google Sheets API](https://console.cloud.google.com/flows/enableapi?apiid=sheets.googleapis.com)

## Configure the OAuth consent screen

1. In the Google Cloud console, go to Menu menu > Google Auth platform > Branding.
[Go to Branding](https://console.cloud.google.com/auth/branding)

2. If you have already configured the Google Auth platform, you can configure the following OAuth Consent Screen settings in Branding, Audience, and Data Access.
3. If you see a message that says Google Auth platform not configured yet, click Get Started:
- Under App Information, in App name, enter a name for the app.
- In User support email, choose a support email address where users can contact you if they have questions about their consent.
- Click Next.
- Under Audience, select External.
- Click Next.
- Under Contact Information, enter an Email address where you can be notified about any changes to your project.
- Click Next.
- Under Finish, review the Google API Services User Data Policy and if you agree, select I agree to the Google API Services: User Data Policy.
- Click Continue.
- Click Create.

## Authorize credentials for an app
To authenticate end users and access user data in your app, you need to create one or more OAuth 2.0 Client IDs. A client ID is used to identify a single app to Google's OAuth servers. If your app runs on multiple platforms, you must create a separate client ID for each platform.

1. In the Google Cloud console, go to Menu menu > Google Auth platform > Clients.
[Go to Clients](https://console.cloud.google.com/auth/clients)
2. Click Create Client.
3. Click Application type > Android app.
4. In the Name field, type a name for the credential. This name is only shown in the Google Cloud console.
5. In the Package Name, type your apps package name find it in your java src files or AndroidManifest.xml
6. In the SHA-1 certificate fingerprint, [follow this guide](https://stackoverflow.com/a/67866731) to get your signing keys fingerprint in android studio/gradle
7. Click Create.

The newly created credential appears under "OAuth 2.0 Client IDs."
