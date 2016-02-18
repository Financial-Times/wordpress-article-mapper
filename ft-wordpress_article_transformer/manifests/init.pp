# == Class: wordpress_article_transformer
#
#
# === Parameters
#
# Document parameters here.
#
# [*sample_parameter*]
#   Explanation of what this parameter affects and what it defaults to.
#   e.g. "Specify one or more upstream ntp servers as an array."
#
# === Variables
#
# Here you should define a list of variables that this module would require.
#
# [*sample_variable*]
#   Explanation of how this variable affects the funtion of this class and if it
#   has a default. e.g. "The parameter enc_ntp_servers must be set by the
#   External Node Classifier as a comma separated list of hostnames." (Note,
#   global variables should not be used in preference to class parameters  as of
#   Puppet 2.6.)
#
# === Examples
#
#  class { wordpress_article_transformer:
#    servers => [ 'pool.ntp.org', 'ntp.local.company.com' ]
#  }
#
# === Authors
#
# Author Name <author@domain.com>
#
# === Copyright
#
# Copyright 2011 Your name here, unless otherwise noted.
#

class wordpress_article_transformer {

  class { "${module_name}::dns": }
  class { "${module_name}::monitoring": }
  class { 'common_pp_up': }
  
  content_runnablejar { "${module_name}_runnablejar":
    service_name => "${module_name}",
    service_description => "${module_name}",
    jar_name => "${module_name}.jar",
    config_file_content => template("${module_name}/config.yml.erb"),
    artifact_location => "${module_name}/${module_name}.jar",
    status_check_url => "http://localhost:8081/ping";
  }

  exec { 'add-wordpress_article_transformer-service':
    command     => "/sbin/chkconfig --add ${module_name}",
    refreshonly => true;
  }
  
  file { "sysconfig":
        path    => "/etc/sysconfig/${module_name}",
        ensure  => 'present',
        content => template("${module_name}/sysconfig.erb"),
        mode    => 644;
  }

Class [ 'common_pp_up' ] ->
Class [ "${module_name}::monitoring" ] ->
Content_runnablejar["${module_name}_runnablejar"]

}
