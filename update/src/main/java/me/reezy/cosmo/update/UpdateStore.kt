package me.reezy.cosmo.update

import com.tencent.mmkv.MMKV
import me.reezy.cosmo.mmkv.MMKVOwner
import me.reezy.cosmo.mmkv.mmkvNullableString
import me.reezy.cosmo.mmkv.mmkvString

object UpdateStore : MMKVOwner {
    override val mmkv: MMKV = MMKV.mmkvWithID("update")

    var ignoreHash by mmkvString()
    var updateHash by mmkvNullableString()
}