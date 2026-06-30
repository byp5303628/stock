import { request } from "@umijs/max";

const prefix = "/api/metadata/indicator";

/**
 * 查询指标字段语义描述。
 */
export async function getIndicatorMeaning(code) {
  return request(`${prefix}/meaning?code=${code}`);
}

/**
 * 查询指标取数规则和使用说明。
 */
export async function getIndicatorUsage(code) {
  return request(`${prefix}/usage?code=${code}`);
}
