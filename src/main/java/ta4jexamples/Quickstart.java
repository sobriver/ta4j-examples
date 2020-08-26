/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2019 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples;

import org.ta4j.core.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.StopGainRule;
import org.ta4j.core.trading.rules.StopLossRule;
import ta4jexamples.loaders.CsvTradesLoader;

/**
 * Quickstart for ta4j.
 *
 * Global example.
 */
public class Quickstart {

    public static void main(String[] args) {

        // 加载数据
        BarSeries series = CsvTradesLoader.loadBitstampSeries();

        // 获取第一日的收盘价
        Num firstClosePrice = series.getBar(0).getClosePrice();
        System.out.println("第一日的收盘价: " + firstClosePrice.doubleValue());
        // 构造一个收盘价的指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        System.out.println(firstClosePrice.isEqual(closePrice.getValue(0))); // equal to firstClosePrice

        //构造一个5日SMA指标(利用收盘价指标构造)
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        System.out.println("在第24处的5日SMA指标值:" + shortSma.getValue(42).doubleValue());

        //构造一个30日SMA指标(利用收盘价指标构造)
        SMAIndicator longSma = new SMAIndicator(closePrice, 30);



        //---------------策略编写方面-------------------------------------
        /**
         * 买入规则定义如下:
         * 1.如果5日SMA值超过30日SMA值
         * 2.或者收盘价低于800
         */
        Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma)
                .or(new CrossedDownIndicatorRule(closePrice, 800));

        // Selling rules
        // We want to sell:
        // - if the 5-bars SMA crosses under 30-bars SMA
        // - or if the price loses more than 3%
        // - or if the price earns more than 2%
        /**
         * 卖出规则定义如下:
         * 1.如果5日SMA值低于30日SMA值
         * 2.或者亏损超过3%(指收盘价)
         * 3.或者盈利超过2%(指收盘价)
         */
        Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma)
                .or(new StopLossRule(closePrice, series.numOf(3))).or(new StopGainRule(closePrice, series.numOf(2)));


        // 开始运行上面编写的策略
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(new BaseStrategy(buyingRule, sellingRule));
        System.out.println("交易次数: " + tradingRecord.getTradeCount());




        // -----------分析-------------------------
        AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
        System.out.println("利润率: " + profitTradesRatio.calculate(series, tradingRecord));
        AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
        System.out.println("风险比: " + rewardRiskRatio.calculate(series, tradingRecord));

        // Total profit of our strategy
        // vs total profit of a buy-and-hold strategy
        AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(new TotalProfitCriterion());
        System.out.println("刚编写的策略与一直持有的策略对比: " + vsBuyAndHold.calculate(series, tradingRecord));

        // Your turn!
    }
}
