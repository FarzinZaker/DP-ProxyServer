package proxy

import weka.classifiers.lazy.IBk
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances

/**
 * Created by root on 8/23/17.
 */
class PredictionModel implements Serializable {

    IBk model
    Integer inputsCount

    void train(List<Integer> arrivalRates, List<Integer> responseTimes, Integer serversCount, Integer next) {

        def inputs = arrivalRates + responseTimes + [serversCount] + [next]
        def currentInputsCount = inputs.size()

        def attributes = new ArrayList<Attribute>()
        inputs.eachWithIndex { input, index ->
            attributes.add(index, new Attribute("Attribute_${index + 1}"))
        }
        Instances structure = new Instances(UUID.randomUUID()?.toString(), attributes, 10000)
        structure.setClassIndex(structure.numAttributes() - 1)

        if (!model || currentInputsCount != inputsCount) {
            inputsCount = currentInputsCount
            model = new IBk()
            model.buildClassifier(structure)
        }

        def instance = new DenseInstance(inputsCount)
        instance.setDataset(structure)
        inputs?.eachWithIndex { Integer input, Integer index ->
            instance.setValue(index, input)
        }

        model.updateClassifier(instance)
    }

    Integer predictNext(List<Integer> arrivalRates, List<Integer> responseTimes, Integer serversCount) {

        def inputs = arrivalRates + responseTimes + [serversCount] + [0]
        def currentInputsCount = inputs.size()

        def attributes = new ArrayList<Attribute>()
        inputs.eachWithIndex { input, index ->
            attributes.add(index, new Attribute("Attribute_${index + 1}"))
        }
        Instances structure = new Instances(UUID.randomUUID()?.toString(), attributes, 10000)
        structure.setClassIndex(structure.numAttributes() - 1)

        if (!model || currentInputsCount != inputsCount)
            return 0

        def instance = new DenseInstance(inputsCount)
        instance.setDataset(structure)
        inputs?.eachWithIndex { Integer input, Integer index ->
            instance.setValue(index, input)
        }

        try {
            Math.round(model.classifyInstance(instance))
        } catch (ignored) {
            1
        }
    }
}
