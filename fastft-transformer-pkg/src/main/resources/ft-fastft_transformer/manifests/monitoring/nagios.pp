class fastft_transformer::monitoring::nagios {
  $thisenv = hiera('lifecycle_environment','')
  nagios::nrpe_checks::check_http{
  "${::certname}/1":
    url           => "http://${::hostname}/healthcheck",
    port          => "8081",
    recode        => "200",
    expect        => 'OK',
    size          => 1,
    wtime         => 2.0,
    action_url    => 'https://sites.google.com/a/ft.com/dynamic-publishing-team/home/fastft-transformer',
    notes         => "Severity 1 \\n Service unavailable \\n FastFT Transformer healthchecks are failing consistently. Please check http://${::hostname}:8081/healthcheck \\n\\n",
    check_interval=> '1',
    ctime         => 2.0;
  }
}
