import { request } from "@umijs/max";

const prefix = "/api/schedule-config"

export async function getAllScheduleConfigs() {
    return request(`${prefix}/list.json`);
}

export async function saveScheduleConfig(data) {
    return request(prefix + "/save.json", {
        method: 'POST',
        data: data
    });
}