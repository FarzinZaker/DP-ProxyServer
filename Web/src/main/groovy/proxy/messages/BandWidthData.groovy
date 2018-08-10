package proxy.messages

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by root on 8/31/17.
 */
class BandWidthData implements Serializable {

    ConcurrentHashMap<String, Float> bandWidths
}
