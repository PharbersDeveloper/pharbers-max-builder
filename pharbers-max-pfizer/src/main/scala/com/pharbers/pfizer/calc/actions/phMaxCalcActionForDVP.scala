package com.pharbers.pfizer.calc.actions

import com.pharbers.pactions.actionbase._
import com.pharbers.spark.phSparkDriver
import org.apache.spark.sql.functions.{col, when, lit}

/**
  * Created by jeorch on 18-5-3.
  */
object phMaxCalcActionForDVP{
    def apply(args: pActionArgs = NULLArgs): pActionTrait = new phMaxCalcActionForDVP(args)
}

class phMaxCalcActionForDVP(override val defaultArgs: pActionArgs) extends pActionTrait {
    override val name: String = "max_calc_action"

    override def perform(pr: pActionArgs): pActionArgs = {

        val job_id = defaultArgs.asInstanceOf[MapArgs].get("job_id").asInstanceOf[StringArgs].get
        lazy val sparkDriver: phSparkDriver = phSparkDriver(job_id)
        import sparkDriver.ss.implicits._

        val panelDF = {
            pr.asInstanceOf[MapArgs].get("panel_data").asInstanceOf[DFArgs].get
                .selectExpr("Date", "Prod_Name", "HOSP_ID", "Sales", "Units")
        }

        val universeDF = {
            pr.asInstanceOf[MapArgs].get("universe_data").asInstanceOf[DFArgs].get
                .withColumnRenamed("PREFECTURE", "City")
                .withColumnRenamed("HOSP_ID", "HOSP_ID_universe")
                .selectExpr("Province", "City", "PHA_HOSP_ID", "MARKET")
        }

        val coefDF = {
            pr.asInstanceOf[MapArgs].get("coef_data").asInstanceOf[DFArgs].get
                .withColumnRenamed("CITY", "City2")
        }

        val panelMergeUniverse = {
            panelDF.join(universeDF, panelDF("HOSP_ID") === universeDF("PHA_HOSP_ID"))
                .withColumn("City2",when(col("City") === "福州市"||col("City") === "厦门市"||col("City") === "泉州市", "福厦泉市")
                    .otherwise(when(col("City") === "珠海市"||col("City") === "东莞市"||col("City") === "中山市"||col("City") === "佛山市", "珠三角市")
                        .otherwise(col("City"))))
        }

        val panelMergeUniverseMergeCoef = {
            panelMergeUniverse.join(coefDF, panelMergeUniverse("City2")===coefDF("City2"), "left")
                .withColumn("coef", when($"coef".isNull, 1.204832714).otherwise($"coef"))
                .withColumn("Sales", when($"Sales" === 0 or $"Units" === 0, 0).otherwise($"Sales"))
                .withColumn("Units", when($"Sales" === 0 or $"Units" === 0, 0).otherwise($"Units"))
                .withColumn("f_sales", $"Sales"*$"coef")
                .withColumn("f_units", $"Units"*$"coef")
        }

        val max_result = {
            panelMergeUniverseMergeCoef.groupBy("Province", "City","Date","Prod_Name","coef", "MARKET")
                .agg(Map("Sales" -> "sum", "Units" -> "sum", "f_sales" -> "sum", "f_units" -> "sum"))
                .withColumnRenamed("sum(Sales)", "Sales")
                .withColumnRenamed("sum(Units)", "Units")
                .withColumnRenamed("sum(f_sales)", "f_sales")
                .withColumnRenamed("sum(f_units)", "f_units")
                .withColumnRenamed("Prod_Name", "Product")
                .withColumn("Panel_ID", lit("none")) // 保证和其他max结果列名一致
                .withColumn("Factor", lit("none")) // 保证和其他max结果列名一致
                .selectExpr("Date", "Province", "City", "Panel_ID", "Product", "Factor", "f_sales", "f_units", "MARKET", "coef", "Sales", "Units") //较其他max结果多出的列放在最后
        }

        DFArgs(max_result)
    }

}