import {request} from '@umijs/max';

let baseUrl;
let mboUrl;
if (process.env.DEPLOY_ENV === 'production') {
  baseUrl = 'https://mpmetadata.sankuai.com';
  mboUrl = 'https://mbo.sankuai.com'
} else if (process.env.DEPLOY_ENV === 'test') {
  baseUrl = 'https://metadata.nibmp.test.sankuai.com';
  mboUrl = 'https://mbo.nibmp.test.sankuai.com';
} else {
  baseUrl = '';
  mboUrl = '';
}

export async function customRequest(url, options) {
  return request(`${baseUrl}${url}`, {
    ...options,
    withCredentials: true,
  });
}

export async function mboRequest(url, options) {
  return request(`${mboUrl}${url}`, {
    ...options,
    withCredentials: true,
  });
}
