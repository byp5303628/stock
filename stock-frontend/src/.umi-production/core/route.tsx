// @ts-nocheck
// This file is generated by Umi automatically
// DO NOT CHANGE IT MANUALLY!
import React from 'react';

export async function getRoutes() {
  const routes = {"1":{"path":"/welcome","name":"welcome","icon":"smile","parentId":"ant-design-pro-layout","id":"1"},"2":{"path":"/strategy","name":"策略","icon":"bulb","parentId":"ant-design-pro-layout","id":"2"},"3":{"path":"/strategy","redirect":"/strategy-list","parentId":"2","id":"3"},"4":{"name":"策略列表","path":"/strategy/strategy-list","parentId":"2","id":"4"},"5":{"path":"/strategy/strategy-list/strategy-detail","parentId":"2","id":"5"},"6":{"path":"/","redirect":"/welcome","parentId":"ant-design-pro-layout","id":"6"},"7":{"path":"*","layout":false,"id":"7"},"ant-design-pro-layout":{"id":"ant-design-pro-layout","path":"/","isLayout":true}} as const;
  return {
    routes,
    routeComponents: {
'1': React.lazy(() => import(/* webpackChunkName: "p__Welcome" */'@/pages/Welcome.jsx')),
'2': React.lazy(() => import( './EmptyRoute')),
'3': React.lazy(() => import( './EmptyRoute')),
'4': React.lazy(() => import(/* webpackChunkName: "p__StrategyList__index" */'@/pages/StrategyList/index.jsx')),
'5': React.lazy(() => import(/* webpackChunkName: "p__StrategyList__StrategyDetail__index" */'@/pages/StrategyList/StrategyDetail/index.jsx')),
'6': React.lazy(() => import( './EmptyRoute')),
'7': React.lazy(() => import(/* webpackChunkName: "p__404" */'@/pages/404.jsx')),
'ant-design-pro-layout': React.lazy(() => import(/* webpackChunkName: "t__plugin-layout__Layout" */'/Users/baiyunpeng04/workspace/stock/stock-frontend/src/.umi-production/plugin-layout/Layout.tsx')),
},
  };
}
