package com.gjvandersloot.service;

import com.gjvandersloot.data.AuthType;

public record VaultKey(String url, AuthType authType) { }
