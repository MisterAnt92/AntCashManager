package com.antcashmanager.domain.model

import com.antcashmanager.architecture.UIEvent

/**
 * None è una classe Kotlin che rappresenta l'assenza di un valore.
 * Estende [UIEvent] per compatibilità con i ViewModel che non gestiscono eventi reali.
 *
 * @constructor Creates a new None
 */
public class None : UIEvent()
