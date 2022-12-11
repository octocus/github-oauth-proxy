# Github-OAuth-Proxy

Hosting your own GitHub OAuth `/access_token` endpoint for your Single Page Application (SPA) to exchange the code for an `access_token`.

## Why needed?

GitHub currently does not support exchanging the code for an access token from a browser.

This means that if you are developing an SPA which purely runs on browser (no backend),
you will get CORS error when requesting `POST https://github.com/login/oauth/access_token`
(as documented in their [doc][1])
from a browser. It is also not a good idea to store `client_secret` on the client side.

This project solves the aforementioned problem by building a thin proxy between your SPA and GitHub's `/login/oauth/access_token` endpoint, where

- [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) is supported. (`Access-Control-Allow-*` headers are set properly.)
- Clients do not need to provide `client_id` and `client_secret` in their requests.

## API

### `HTTP` payload

After a user is redirected back to your site by GitHub successfully,
instead of requesting GitHub according to ["Step 2"][1],
request the API of this service like below.

```text
POST ${YOUR_HOST_NAME}/access_token?code=${oauthCode}
```

The response payload is expected to be [the same](https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps#response) as what is returned from GitHub.

### `axis`
```typescript
const params = new URLSearchParams();
params.append('code', oauthCode);
return await axios.post(`${YOUR_HOST_NAME}/access_token`, params, {
  headers: {
    'Accept': 'application/json'
  }
});
```

## Deployment

### Docker

```shell
docker run -d \
  -e GITHUB_CLIENT_ID="${GITHUB_CLIENT_ID}" \
  -e GITHUB_CLIENT_SECRET="${GITHUB_CLIENT_SECRET}" \
  -p 8080:8080 \
  zetaplusae/github-oauth-proxy
```

### DigitalOcean

[![Deploy to DigitalOcean](https://www.deploytodo.com/do-btn-blue.svg)](https://cloud.digitalocean.com/apps/new?repo=https://github.com/octocus/github-oauth-proxy/tree/main&refcode=a046a1feb184)

> Note:
> 
> Do not forget to *edit* and provide the following *Environment Variables* for the *web* service on the second step:
> - `GITHUB_CLIENT_ID`
> - `GITHUB_CLIENT_SECRET`

## Other configurations

You can also customize the service by providing the environment variables listed below.

| Env Name                | Required | Default                                       | Description                                                                                           |
|-------------------------|----------|-----------------------------------------------|-------------------------------------------------------------------------------------------------------|
| GITHUB_CLIENT_ID        | YES      | N/A                                           | The `client_id` of your OAuth application registered on GitHub. (See: [Creating an OAuth App][2])     |
| GITHUB_CLIENT_SECRET    | YES      | N/A                                           | The `client_secret` of your OAuth application registered on GitHub. (See: [Creating an OAuth App][2]) |
| GITHUB_ACCESS_TOKEN_URL | NO       | `https://github.com/login/oauth/access_token` | The URL endpoint to exchange a code for an access token.                                              |
| HTTP_PORT               | NO       | `8080`                                        | The port of the HTTP Server inside the container.                                                     |
| ACCESS_TOKEN_PATH       | NO       | `/access_token`                               | The path that serves the request.                                                                     |


[1]: https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps#2-users-are-redirected-back-to-your-site-by-github
[2]: https://docs.github.com/en/developers/apps/building-oauth-apps/creating-an-oauth-app
