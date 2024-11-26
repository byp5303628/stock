const {DEPLOY_ENV = 'test'} = process.env;
const Settings = {
  navTheme: 'light',
  // 拂晓蓝
  colorPrimary: '#1890ff',
  layout: 'mix',
  contentWidth: 'Fluid',
  fixedHeader: false,
  fixSiderbar: true,
  colorWeak: false,
  title: '股票策略中心',
  pwa: true,
  logo: DEPLOY_ENV === 'production' ?
    'https://s3plus.sankuai.com/v1/mss_e4b9616ba5ac4bd89b55664672fdf747/MWS-SDK/static-images/MWSlogo_yellow.png' :
    'https://s3plus.sankuai.com/v1/mss_e4b9616ba5ac4bd89b55664672fdf747/MWS-SDK/static-images/MWSlogo_blue.png',
  iconfontUrl: '',
  token: {
    // 参见ts声明，demo 见文档，通过token 修改样式
    //https://procomponents.ant.design/components/layout#%E9%80%9A%E8%BF%87-token-%E4%BF%AE%E6%94%B9%E6%A0%B7%E5%BC%8F
  },
};
export default Settings;
