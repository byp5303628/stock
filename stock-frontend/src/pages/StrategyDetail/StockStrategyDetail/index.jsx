import { PageContainer } from "@ant-design/pro-components";
import { Card, Col, Descriptions, Row, Table, Tag } from "antd";
import { useState, useEffect } from 'react'; // 引入 useState 和 useEffect
import { useSearchParams } from 'umi'; // 引入 useSearchParams
import { getStockPredictIndicator } from "@/services/strategy";
import { BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts';



const StockStrategyDetail = () => {

    const [stockPredictIndicator, setStockPredictIndicator] = useState({});
    const [tradeCycleList, setTradeCycleList] = useState([]);
    const [policyDetail, setPolicyDetail] = useState({});

    const [searchParams] = useSearchParams();
    const code = searchParams.get("code");
    const name = searchParams.get("name");

    useEffect(() => {
        getStockPredictIndicator({
            code: code,
            name: name
        }).then(r => {
            if (r && r.code === 200) {
                setStockPredictIndicator(r.data.stockPredictIndicator);
                setTradeCycleList(r.data.tradeCycles);
                setPolicyDetail(r.data.strategyDetailDTO);
            }
        });
    }, []);

    const DurationHistogram = ({ tradeCycleList }) => {
        const data = tradeCycleList.map(cycle => ({
            name: cycle.purchaseDate,
            duration: cycle.goldDays,
        }));

        return (
            <BarChart width={600} height={300} data={data}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="duration" fill="#8884d8" />
            </BarChart>
        );
    };

    const ProfitHistogram = ({ tradeCycleList }) => {
        const data = tradeCycleList.map(cycle => ({
            name: cycle.purchaseDate,
            profit: (cycle.increase * 100).toFixed(2),
        }));

        return (
            <BarChart width={600} height={300} data={data}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="profit" fill="#82ca9d" />
            </BarChart>
        );
    };

    const tradeCycleColumns = [
        {
            title: '购买日期',
            dataIndex: 'purchaseDate',
            key: 'purchaseDate',
            sorter: (a, b) => a.purchaseDate.localeCompare(b.purchaseDate), // 根据购买日期排序
        },
        {
            title: '卖出日期',
            dataIndex: 'saleDate',
            key: 'saleDate',
            sorter: (a, b) => a.saleDate.localeCompare(b.saleDate), // 根据卖出日期排序
        },
        {
            title: '增幅',
            dataIndex: 'increase',
            key: 'increase',
            render: (increase) => {
                return <span>{(increase * 100).toFixed(2)}%</span>; // 显示为百分比
            },
            sorter: (a, b) => a.increase - b.increase, // 根据增幅排序
        },
        {
            title: '持续天数',
            dataIndex: 'goldDays',
            key: 'goldDays',
            sorter: (a, b) => a.goldDays - b.goldDays, // 根据持续天数排序
        },
        {
            title: '购买价格',
            dataIndex: 'purchasePrice',
            key: 'purchasePrice',
            sorter: (a, b) => a.purchasePrice - b.purchasePrice, // 根据购买价格排序
        },
        {
            title: '销售价格',
            dataIndex: 'salePrice',
            key: 'salePrice',
            sorter: (a, b) => a.salePrice - b.salePrice, // 根据销售价格排序
        }
    ];

    return (
        <PageContainer title={"股票策略-个股详情-" + code}
            content
        >
            <Card title="个股详情-基本信息">
                <Descriptions>
                    <Descriptions.Item label="策略名称">{policyDetail.name}</Descriptions.Item>
                    <Descriptions.Item label="标签">
                        {policyDetail.tags && policyDetail.tags.length > 0 ? (
                            policyDetail.tags.map((tag, index) => (
                                <Tag key={index}>{tag}</Tag>
                            ))
                        ) : (
                            '无'
                        )}
                    </Descriptions.Item>
                    <Descriptions.Item label="描述">{policyDetail.description}</Descriptions.Item>
                    <Descriptions.Item label="验证股票数量">{policyDetail.verifyStockCnt}</Descriptions.Item>
                    <Descriptions.Item label="总股票数量">{policyDetail.totalStockCnt}</Descriptions.Item>
                    <Descriptions.Item label="验证率">{policyDetail.verifyRate}</Descriptions.Item>
                </Descriptions>
            </Card>

            <Card title="个股详情-统计概览" style={{ marginTop: 16 }}>
                <Descriptions>
                    <Descriptions.Item label="开始日期">{stockPredictIndicator.startDate}</Descriptions.Item>
                    <Descriptions.Item label="结束日期">{stockPredictIndicator.endDate}</Descriptions.Item>
                    <Descriptions.Item label="金叉数量">{stockPredictIndicator.goldCycleCnt}</Descriptions.Item>
                    <Descriptions.Item label="实际总增幅">{(stockPredictIndicator.increaseActualTotal * 100).toFixed(2)}%</Descriptions.Item>
                    <Descriptions.Item label="算数总增幅">{(stockPredictIndicator.increaseTotal * 100).toFixed(2)}%</Descriptions.Item>
                    <Descriptions.Item label="实际平均增幅">{(stockPredictIndicator.increaseActualAvg * 100).toFixed(2)}%</Descriptions.Item>
                    <Descriptions.Item label="算数平均增幅">{(stockPredictIndicator.increaseAvg * 100).toFixed(2)}%</Descriptions.Item>
                    <Descriptions.Item label="周期最大增幅">{(stockPredictIndicator.increaseMax * 100).toFixed(2)}%</Descriptions.Item>
                    <Descriptions.Item label="周期最小增幅">{(stockPredictIndicator.increaseMin * 100).toFixed(2)}%</Descriptions.Item>
                    <Descriptions.Item label="月增幅">{(stockPredictIndicator.monthIncrease * 100).toFixed(2)}%</Descriptions.Item>
                    <Descriptions.Item label="年增幅">{(stockPredictIndicator.yearIncrease * 100).toFixed(2)}%</Descriptions.Item>
                </Descriptions>
            </Card>

            <Card title="持续天数和增长分布" style={{ marginTop: 16 }}>
                <Row>
                    <Col span={12}>
                        <Card title="持续天数分布" style={{ margin: 4 }}>
                            <DurationHistogram tradeCycleList={tradeCycleList} />
                        </Card>
                    </Col>
                    <Col span={12}>
                        <Card title="增长分布" style={{ margin: 4 }}>
                            <ProfitHistogram tradeCycleList={tradeCycleList} />
                        </Card>

                    </Col>
                </Row>
            </Card>

            <Card title="个股详情-交易周期详情" style={{ marginTop: 16 }}>
                <Table
                    columns={tradeCycleColumns}
                    dataSource={tradeCycleList}></Table>
            </Card>
        </PageContainer>
    );
}

export default StockStrategyDetail;