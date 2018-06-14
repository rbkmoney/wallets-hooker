package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.Hook;

import java.util.List;

/**
 * Created by inal on 28.11.2016.
 */
public interface HookDao {
    List<Hook> getPartyHooks(String partyId) throws DaoException;
    Hook getHookById(long id) throws DaoException;
    Hook create(Hook hook) throws DaoException;
    void delete(long id) throws DaoException;
}
