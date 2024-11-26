import { PageContainer } from "@ant-design/pro-components";
import { Card, Descriptions, Divider, Table } from "antd";

const StrategyList = () => {
    const columns = [
        {
            title: '策略',
            dataIndex: 'name',
            key: 'id',
        },
        {
            title: '策略类型',
            dataIndex: 'name',
            key: 'id',
        },
        {
            title: '策略中文名',
            dataIndex: 'name',
            key: 'id',
        },
        {
            title: '策略描述',
            dataIndex: 'name',
            key: 'id',
        },
        {
            title: '操作',
            dataIndex: 'name',
            key: 'id',
        },
    ];

    return (
        <PageContainer
            title={"策略说明"}
            content={"本页包含目前支持的全部策略, 并且包含每个策略中对于股票的统计信息"}
        >
            <Table columns={columns} dataSource={[]} />
        </PageContainer>);
}

export default StrategyList;