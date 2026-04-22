import React from "react";
import { Row, Col, Card, Statistic } from "antd";

const styles = {
  card: {
    background: "rgba(255,255,255,0.03)",
    backdropFilter: "blur(12px)",
    border: "1px solid rgba(255,255,255,0.08)",
    borderRadius: 16,
  },
};

const Stats = () => {
  const items = [
    { title: "Lượt ra", value: 123 },
    { title: "Lượt vào", value: 450 },
    { title: "Hiện đang đỗ", value: 327 },
  ];

  return (
    <Row gutter={16} style={{ marginTop: 20 }}>
      {items.map((item, i) => (
        <Col span={8} key={i}>
          <Card style={styles.card}>
            <Statistic
              title={<span style={{ color: "#aaa" }}>{item.title}</span>}
              value={item.value}
            />
          </Card>
        </Col>
      ))}
    </Row>
  );
};

export default Stats;
