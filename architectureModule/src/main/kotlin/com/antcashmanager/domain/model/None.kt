package com.antcashmanager.domain.model

import com.antcashmanager.architecture.UIEvent

/** Sentinel no-op per i ViewModel che non gestiscono eventi reali. Singleton. */
public object None : UIEvent()
