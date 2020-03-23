- First registration of control with nickname, devicename, no token, no challenge code (user:password in format :XXXX)
  - Non 401 error Code
    - control cannot be registered (control must be deleted)
    - resposne:   {"error":[42000,"not register any more"],"id":8}
  - 401-response
    - Navigate to enter challenge code view
    - Enter challenge code
    - Send request with nickname, devicename, no token, entered challenge code
- Result OK
  - Response contains auth token in Set-Cookie header
  - Any further request can be performed using nickname, devicename, token
- If token expires -> response 401
  - Register with nickname, devicename, expired toekn
  - Receives fresh token from response (set-cookie)


 if (uuid == null || uuid.length() == 0) {
            uuid = UUID.randomUUID().toString();
        }
        SonyJsonRpcResponse response = SonyJsonRpc.actRegister(getBaseUrl(),
                nickname + ":" + uuid,
                nickname + " (" + devicename + ")",
                cookie,
                challenge == null || challenge.isEmpty() ? null : ":" + challenge
                );

public static SonyJsonRpcResponse actRegister(
            String baseUrl, String clientd, String nickname, String cookieString, String userPassword) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("actRegister", 8)
                .addParam("clientid", clientd)
                .addParam("nickname", nickname)
                .addParam("level", "private")
                .addParamListItem()
                .addParamToNewList("value", "yes")
                .addParamToNewList("function", "WOL");
        return execute(request, baseUrl + "/accessControl", cookieString, userPassword);
    }

if(cookieString!=null && !cookieString.isEmpty()) {
                connection.setRequestProperty("Cookie", cookieString);
            }
            if(userPassword!=null && !userPassword.isEmpty()) {
                connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(userPassword.getBytes()));
            }

Token handling:
- Principle: Domain model only in repository
- In repository:
  - sonyControls = controlStore.loadControls() // get control from preferences
  - token = controlStore.loadToken() // get latest stored token from preferences
  - updated selected Control with latest token value
  - on any change of sonyControls:
    - before change:
      - get latest token for selected control by controlStore.loadToken()
      - updated selected Control with latest token value
    - apply change to selected controls
    - save changes in store by controlStore.storeControls(controls)
    - token = selectedControl.cookie // get token of new control
    - controlStore.storeToken(token) -> store token of selected control in token preference
