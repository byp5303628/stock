import React from 'react';
import { Tabs } from 'antd';
import { BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

const { TabPane } = Tabs;

const StrategyAnalysis = ({ stockPredictIndicators }) => {
    // 假设 stockPredictIndicators 是一个数组，包含每支股票的收益和金叉个数
    const profitData = stockPredictIndicators.map(item => ({
        key: item.code,
        name: item.code,
        profit: (item.increaseActualTotal * 100).toFixed(2), // 收益
    }));

    const goldenCrossData = stockPredictIndicators.map(item => ({
        key: item.code,
        name: item.code,
        count: item.goldCycleCnt, // 金叉个数
    }));

    return (
        <Tabs defaultActiveKey="1">
            <TabPane tab="收益分布" key="1">
                <BarChart width={1200} height={300} data={profitData}>
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <CartesianGrid strokeDasharray="3 3" />
                    <Bar dataKey="profit" fill="#8884d8" />
                </BarChart>
            </TabPane>
            <TabPane tab="金叉个数" key="2">
                <BarChart width={1200} height={300} data={goldenCrossData}>
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <CartesianGrid strokeDasharray="3 3" />
                    <Bar dataKey="count" fill="#82ca9d" />
                </BarChart>
            </TabPane>
        </Tabs>
    );
};

export default StrategyAnalysis;