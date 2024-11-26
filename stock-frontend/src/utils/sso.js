import SSOWeb from '@mtfe/sso-web';

/**
 * @see 配置参考 http://docs.sankuai.com/doc/it/web-sso-sdk/configuration/
 */
export const ssoOption = {
  clientId: 'f7cbaf6487',
  accessEnv: 'product', // 线下(beta,dev,test) 用"test", 线上(product,staging) 用"product"
  // 如果使用hash路由，需修改为hash路由风格，如/#/sso/callback
  // callbackUrl: "/#/sso/callback",
  // 如果应用使用hash路由，需修改为hash路由风格，如/#/sso/logout
  // logoutUri: "/#/sso/logout",
};

const ssoWeb = SSOWeb(ssoOption);
export default ssoWeb;
