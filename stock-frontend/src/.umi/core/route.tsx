// @ts-nocheck
// This file is generated by Umi automatically
// DO NOT CHANGE IT MANUALLY!
import React from 'react';

export async function getRoutes() {
  const routes = {"1":{"path":"/welcome","name":"welcome","icon":"smile","parentId":"ant-design-pro-layout","id":"1"},"2":{"path":"/strategy-list","name":"策略列表","icon":"bulb","parentId":"ant-design-pro-layout","id":"2"},"3":{"path":"/","redirect":"/welcome","parentId":"ant-design-pro-layout","id":"3"},"4":{"path":"*","layout":false,"id":"4"},"ant-design-pro-layout":{"id":"ant-design-pro-layout","path":"/","isLayout":true},"umi/plugin/openapi":{"path":"/umi/plugin/openapi","id":"umi/plugin/openapi"}} as const;
  return {
    routes,
    routeComponents: {
'1': React.lazy(() => import(/* webpackChunkName: "p__Welcome" */'@/pages/Welcome.jsx')),
'2': React.lazy(() => import(/* webpackChunkName: "p__StrategyList__index" */'@/pages/StrategyList/index.jsx')),
'3': React.lazy(() => import( './EmptyRoute')),
'4': React.lazy(() => import(/* webpackChunkName: "p__404" */'@/pages/404.jsx')),
'ant-design-pro-layout': React.lazy(() => import(/* webpackChunkName: "umi__plugin-layout__Layout" */'/Users/baiyunpeng04/workspace/stock/stock-frontend/src/.umi/plugin-layout/Layout.tsx')),
'umi/plugin/openapi': React.lazy(() => import(/* webpackChunkName: "umi__plugin-openapi__openapi" */'/Users/baiyunpeng04/workspace/stock/stock-frontend/src/.umi/plugin-openapi/openapi.tsx')),
},
  };
}