import { PageContainer } from "@ant-design/pro-components";
import { Card, Descriptions, Divider, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { getStockStrategyList } from "@/services/strategy";

const StrategyList = () => {

    const [strategyList, setStrategyList] = useState([])

    useEffect(() => {
        getStockStrategyList().then(r => {
            if (r && r.code === 200) {
                setStrategyList(r.data);
            }
        });
    }, []);

    const columns = [
        {
            title: '策略',
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: '策略标签',
            dataIndex: 'tags',
            key: 'tags',
            render: (tags) => {
                if (!tags) {
                    return '';
                }
                const t = [];

                tags.forEach(i => {
                    t.push(<Tag>{i}</Tag>)
                })
                return <div>
                    {t}
                </div>;
            }
        },
        {
            title: '策略中文名',
            dataIndex: 'description',
            key: 'description',
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
        },
        {
            title: '操作',
            dataIndex: 'name',
            key: 'name',
            render: (name) => {
                return <a href={`#/strategy/strategy-list/strategy-detail?name=${name}`}>查看详情</a>
            }
        },
    ];

    return (
        <PageContainer
            title={"策略说明"}
            content={"本页包含目前支持的全部策略, 并且包含每个策略中对于股票的统计信息"}
        >
            <Table columns={columns} dataSource={strategyList}/>
        </PageContainer>);
}

export default StrategyList;