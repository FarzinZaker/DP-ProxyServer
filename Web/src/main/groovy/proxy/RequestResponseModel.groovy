package proxy

import org.encog.Encog
import org.encog.mathutil.randomize.XaiverRandomizer
import org.encog.ml.data.MLDataSet
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.training.propagation.sgd.StochasticGradientDescent
import org.encog.neural.networks.training.propagation.sgd.update.AdamUpdate
import org.encog.neural.networks.training.propagation.sgd.update.MomentumUpdate
import org.encog.util.Format
import org.encog.util.simple.EncogUtility

/**
 * Created by root on 8/15/17.
 */
class RequestResponseModel {

    BasicNetwork network
    StochasticGradientDescent sgd
    Integer maxArrivalRate
    Integer maxResponseTime

    RequestResponseModel(Integer maxArrivalRate, Integer maxResponseTime){

        this.maxArrivalRate = maxArrivalRate
        this.maxResponseTime = maxResponseTime
    }

    void train(List<Integer> arrivalRates, List<Integer> responseTimes, Integer nextArrivalRate, Integer nextResponseTime) {

        def inputsCount = (arrivalRates + responseTimes).size()
        def input = new double[1][inputsCount]
        (arrivalRates?.collect { it / maxArrivalRate } + responseTimes?.collect { it / maxResponseTime }).eachWithIndex { item, index -> input[0][index] = item }
        MLDataSet trainingSet = new BasicMLDataSet(input as double[][], [[nextArrivalRate / maxArrivalRate, nextResponseTime / maxResponseTime]] as double[][])

        if (!network || network?.inputCount != inputsCount) {
            network = EncogUtility.simpleFeedForward(inputsCount, 10, 4, 2, false)
//            new XaiverRandomizer(42).randomize(network)

            sgd = new StochasticGradientDescent(network, trainingSet)
//            sgd.setLearningRate(0.1)
//            sgd.setMomentum(0.9)
            sgd.setUpdateRule(new MomentumUpdate())
        } else {
            sgd.process(trainingSet.get(0))
            sgd.update()
        }
//        def error = network.calculateError(trainingSet)
//        println error
//        System.out.println("Step #" + sgd.getIteration() + ", Step Error: "
//                + Format.formatDouble(sgd.getError(), 2) + ", Global Error: "
//                + Format.formatDouble(error, 2))
//        EncogUtility.evaluate(network, trainingSet)
//        Encog.getInstance().shutdown()
    }

    Map<String, Integer> predict(List<Integer> arrivalRates, List<Integer> responseTimes) {
        def inputsCount = (arrivalRates + responseTimes).size()
        if (!network || inputsCount != network.inputCount)
            return [
                    nextArrivalRate : 0,
                    nextResponseTime: 0
            ]
        def input = new BasicMLData(inputsCount)
        (arrivalRates?.collect { it / maxArrivalRate } + responseTimes?.collect { it / maxResponseTime }).eachWithIndex { item, index -> input.setData(index, item) }
        def output = network.compute(input)
        [
                nextArrivalRate : Math.round(output.getData(0) * maxArrivalRate) as Integer,
                nextResponseTime: Math.round(output.getData(1) * maxResponseTime) as Integer
        ]
    }

    Integer predictNextArrivalRate(List<Integer> arrivalRates, List<Integer> responseTimes) {
        predict(arrivalRates, responseTimes).nextArrivalRate
    }

    Integer predictNextResponseTime(List<Integer> arrivalRates, List<Integer> responseTimes) {
        predict(arrivalRates, responseTimes).nextResponseTime
    }
}
