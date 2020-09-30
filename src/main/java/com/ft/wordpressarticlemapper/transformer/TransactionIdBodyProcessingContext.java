package com.ft.wordpressarticlemapper.transformer;

import com.ft.bodyprocessing.BodyProcessingContext;

public interface TransactionIdBodyProcessingContext extends BodyProcessingContext {

  String getTransactionId();
}
