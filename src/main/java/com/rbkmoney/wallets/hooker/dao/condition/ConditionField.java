package com.rbkmoney.wallets.hooker.dao.condition;

import org.jooq.Comparator;
import org.jooq.Field;

public interface ConditionField<F, V> {

    Field<F> getField();

    V getValue();

    Comparator getComparator();

}
