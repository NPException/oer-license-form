var ##object_var## = {
  setLicenseName: function(v) {
    document.getElementById('##id_license_name##').value = v;
  },
  
  setLicenseText: function(v) {
    document.getElementById('##id_license_text##').value = v;
  },
  
  chooseNoLicense: function(){
    ##object_var##.setLicenseName('');
    ##object_var##.setLicenseText('');
  },
  
  chooseCCLicense: function(){
    ##object_var##.setLicenseName('');
    ##object_var##.setLicenseText('');
  },
  
  chooseFreeLicense: function(){
    ##object_var##.setLicenseName('CC0 / Public Domain');
    ##object_var##.setLicenseText('');
  },
  
  chooseCustomLicense: function(){
    ##object_var##.setLicenseName('Eigene Lizenz');
    ##object_var##.setLicenseText(
      document.getElementById('##id_prefix##custom-input').value)
  },
  
  onBaseChanged: function(){
    var e = document.getElementById('##id_prefix##base');
    var s = e.options[e.selectedIndex].value;
    var all = document.querySelectorAll('[id^=\"##id_prefix##part-\"]');
    for(i=0; i<all.length; i++){
      all[i].style.display='none';
    }
    document.getElementById('##id_prefix##part-'+s).style.display='';
    var map = {
      none: ##object_var##.chooseNoLicense,
      cc: ##object_var##.chooseCCLicense,
      free: ##object_var##.chooseFreeLicense,
      custom: ##object_var##.chooseCustomLicense
    };
    map[s]();
  }
}
