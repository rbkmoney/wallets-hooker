package com.rbkmoney.wallets_hooker.utils;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;

import java.util.List;
import java.util.stream.Collectors;

public class LogUtils {

    public static String getLogWebHookModel(List<WebHookModel> webHookModels) {
        return webHookModels.stream()
                .map(WebHookModel::toString)
                .collect(Collectors.joining(", "));
    }
}
