class wordpress_article_transformer::dns {
    
    # if this is a non-production server, set the server to use production DNS
    # this is to work around content not existing in int or test
    # which causes redirects and healthchecks to fail
    
    case "${::ft_environment}" {
        'd', 'qa', 'int', 't': {
            
            file {
                '/etc/resolv.conf':
                    ensure  => present,
                    source  => "puppet:///modules/$module_name/resolv.conf",
                    owner   => "root",
                    group   => "root";
           
                '/etc/profile.d/z_dns_warning.sh':
                    ensure  => present,
                    source  => "puppet:///modules/$module_name/z_dns_warning.sh",
                    owner   => "root",
                    group   => "root";
            }
        }
    }
}
