import { PageContainer } from "@ant-design/pro-components";
import { useEffect, useState } from "react";
import { getStockStrategyByName, startTradeRegression } from "@/services/strategy";
import { useSearchParams } from "umi";
import { Button, Card, Descriptions, message, Table } from "antd";
import StrategyAnalysis from "@/pages/StrategyDetail/StrategyAnalysis";

const StrategyDetail = () => {

    const [searchParams] = useSearchParams();
    const [strategy, setStrategy] = useState({});
    const [indicators, setIndicators] = useState([]);
    const [stockPredictIndicators, setStockPredictIndicators] = useState([]);

    const name = searchParams.get("name");

    useEffect(() => {
        getStockStrategyByName(name).then(r => {
            if (r && r.code === 200) {
                setStrategy(r.data);
                setIndicators(r.data.indicators);
                setStockPredictIndicators(r.data.stockPredictIndicators);
            }
        });
    }, []);

    const columns = [
        {
            title: '代码',
            dataIndex: 'code',
            key: 'code',
        },
        {
            title: '名称',
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: '金叉个数',
            dataIndex: 'goldCycleCnt',
            key: 'goldCycleCnt',
            sorter: (a, b) => a.goldCycleCnt - b.goldCycleCnt,
        },
        {
            title: '策略起始日期',
            dataIndex: 'startDate',
            key: 'startDate',
            sorter: (a, b) => new Date(a.startDate) - new Date(b.startDate),
        },
        {
            title: '策略结束日期',
            dataIndex: 'endDate',
            key: 'endDate',
            sorter: (a, b) => new Date(a.endDate) - new Date(b.endDate),
        },
        {
            title: '总盈利总值',
            dataIndex: 'increaseActualTotal',
            key: 'increaseActualTotal',
            sorter: (a, b) => a.increaseActualTotal - b.increaseActualTotal,
            render: (txt) => {
                return <span>{(txt * 100).toFixed(2)}%</span>; // 显示为百分比
            }
        },
        {
            title: '盈利实际平均',
            dataIndex: 'increaseActualAvg',
            key: 'increaseActualAvg',
            sorter: (a, b) => a.increaseActualAvg - b.increaseActualAvg,
            render: (txt) => {
                return <span>{(txt * 100).toFixed(2)}%</span>; // 显示为百分比
            }
        },
        {
            title: '单周期最大增长',
            dataIndex: 'increaseMax',
            key: 'increaseMax',
            sorter: (a, b) => a.increaseMax - b.increaseMax,
            render: (txt) => {
                return <span>{(txt * 100).toFixed(2)}%</span>; // 显示为百分比
            }
        },
        {
            title: '月均增长',
            dataIndex: 'monthIncrease',
            key: 'monthIncrease',
            sorter: (a, b) => a.monthIncrease - b.monthIncrease,
            render: (txt) => {
                return <span>{(txt * 100).toFixed(2)}%</span>; // 显示为百分比
            }
        },
        {
            title: '年均增长',
            dataIndex: 'yearIncrease',
            key: 'yearIncrease',
            sorter: (a, b) => a.yearIncrease - b.yearIncrease,
            render: (txt) => {
                return <span>{(txt * 100).toFixed(2)}%</span>; // 显示为百分比
            }
        },
        {
            title: '操作',
            dataIndex: 'code',
            key: 'code',
            render: (code) => {
                return (
                    <a href={`/#/strategy/strategy-list/strategy-detail/stock-strategy-detail?name=${name}&code=${code}`}>详情</a>
                );
            }
        }
    ];

    const renderIndicators = () => {
        return indicators.map((indicator, index) => {
            let val = indicator.value;

            if (!val) {
                return null;
            }

            if (indicator.type === "Double") {
                val = val !== 'Infinity' ? indicator.value.toFixed(2) : 'Infinity';
            } else if (indicator.type === "Percent") {
                val = (indicator.value * 100).toFixed(2) + "%";
            }

            return (
                <Descriptions.Item label={indicator.description} key={index}>
                    {val}
                </Descriptions.Item>
            )
        })
    }

    return (
        <PageContainer
            title={name}
            content={strategy.description}
            extra={<Button type={"primary"} onClick={() => {
                startTradeRegression(name).then(r => {
                    if (r && r.code === 200) {
                        message.success("开始回溯!");
                    } else {
                        message.warning("系统繁忙, 请稍后重试!")
                    }
                })
            }}>开始回溯</Button>}
        >
            <Card title={"总体统计"}>
                <Descriptions>
                    {renderIndicators()}
                </Descriptions>
            </Card>
            <Card title={"策略分析"} style={{ marginTop: 8 }}>
                <StrategyAnalysis stockPredictIndicators={stockPredictIndicators} />
            </Card>
            <Card title={"个股详情"} style={{ marginTop: 8 }}>
                <Table columns={columns} dataSource={stockPredictIndicators} />
            </Card>
        </PageContainer>
    );
}

export default StrategyDetail;
