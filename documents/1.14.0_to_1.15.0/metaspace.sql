ALTER TABLE api  ADD  api_key varchar(255);
comment on column api.api_key is 'api一键测试验证参数';
