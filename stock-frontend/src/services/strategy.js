import { request } from "@umijs/max";

export async function getStockStrategyList() {
    return request("/api/stock-strategy/list.json");
}

export async function getStockStrategyByName(name) {
    return request(`/api/stock-strategy/detail.json?name=${name}`);
}