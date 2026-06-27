import { request } from "@umijs/max";

// ===== 模型管理 =====
export async function listModels() {
  return request("/api/metadata/model/list.json");
}

export async function getModelDetail(id) {
  return request(`/api/metadata/model/detail.json?id=${id}`);
}

export async function saveModel(data) {
  return request("/api/metadata/model/save.json", { method: 'POST', data });
}

export async function validateModel(data) {
  return request("/api/metadata/model/validate.json", { method: 'POST', data });
}

// ===== 字段管理 =====
export async function saveField(data) {
  return request("/api/metadata/field/save.json", { method: 'POST', data });
}

export async function deleteField(id) {
  return request(`/api/metadata/field/delete.json?id=${id}`, { method: 'DELETE' });
}

// ===== 枚举管理 =====
export async function listEnums() {
  return request("/api/metadata/enum/list.json");
}

export async function getEnumDetail(id) {
  return request(`/api/metadata/enum/detail.json?id=${id}`);
}

export async function saveEnum(data) {
  return request("/api/metadata/enum/save.json", { method: 'POST', data });
}

/**
 * C5: 真正的枚举删除接口，有引用时后端返回错误。
 */
export async function deleteEnum(id) {
  return request(`/api/metadata/enum/delete.json?id=${id}`, { method: 'DELETE' });
}

export async function bindEnum(data) {
  return request("/api/metadata/enum/bind.json", { method: 'POST', data });
}

export async function unbindEnum(data) {
  return request("/api/metadata/enum/unbind.json", { method: 'POST', data });
}

export async function getEnumUsage(id) {
  return request(`/api/metadata/enum/usage.json?id=${id}`);
}

// ===== 集成 API =====

/**
 * C2: URL 已修正，去掉重复的 /indicator 前缀。
 * 实际路径: /api/metadata/indicator/meaning
 */
export async function getIndicatorMeaning(code) {
  return request(`/api/metadata/indicator/meaning?code=${code}`);
}

/**
 * C2: URL 已修正，去掉重复的 /indicator 前缀。
 * 实际路径: /api/metadata/indicator/usage
 */
export async function getIndicatorUsage(code) {
  return request(`/api/metadata/indicator/usage?code=${code}`);
}
