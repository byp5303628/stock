import { PageContainer } from "@ant-design/pro-components";
import { useEffect, useState } from "react";
import { getStockStrategyByName, startTradeRegression } from "@/services/strategy";
import {useSearchParams} from "umi";
import { Button, Card, message } from "antd";

const StrategyDetail = () => {

    const [searchParams] = useSearchParams();
    const [strategy, setStrategy] = useState({});

    const name = searchParams.get("name");

    useEffect(() => {
        getStockStrategyByName(name).then(r => {
            if (r && r.code === 200) {
                setStrategy(r.data);
            }
        })
    }, []);

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

            </Card>
            <Card title={"个股详情"} style={{marginTop: 8}}>

            </Card>
        </PageContainer>
    );
}

export default StrategyDetail;

