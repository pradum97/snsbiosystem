package com.techwhizer.snsbiosystem;

import com.techwhizer.snsbiosystem.app.AppConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

public class Main extends Application {
    public static Stage primaryStage;

    static String token = "AuthToken=d/d5VECDlIyAEGdTnIfU1sbXW+IjoSYcnj7Bc3AxF8AmlO9TWIZZta6Xx0eEki7a9GoT/De4js/82X1PcrMTuCHrqEb0EGrHzWQTLDaQeLRCMQlTM7PDyxSwt5yDfLowl4/HUtwXEMKIJWWRWMylWhneVV+JsEMlll5qIpx6CCDaIHFExJ00hwEULDXctrPsTXc+/g7LEfA/g63bvIxs7YN6QR87q9R5prFDNpzh0wo5lcZlbN2PZgYcWQvzMNca; Path=/; Domain=localhost; HttpOnly;";
    public static String data = "JVBERi0xLjUKJeLjz9MKMyAwIG9iago8PC9Db2xvclNwYWNlL0RldmljZVJHQi9TdWJ0eXBlL0ltYWdlL0hlaWdodCA4MDIvRmlsdGVyL0RDVERlY29kZS9UeXBlL1hPYmplY3QvV2lkdGggNTU1L0JpdHNQZXJDb21wb25lbnQgOC9MZW5ndGggMTczMTI+PnN0cmVhbQr/2P/gABBKRklGAAEBAQBgAGAAAP/hACJFeGlmAABNTQAqAAAACAABARIAAwAAAAEAAQAAAAAAAP/bAEMAAgEBAgEBAgICAgICAgIDBQMDAwMDBgQEAwUHBgcHBwYHBwgJCwkICAoIBwcKDQoKCwwMDAwHCQ4PDQwOCwwMDP/bAEMBAgICAwMDBgMDBgwIBwgMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDP/AABEIAyICKwMBIgACEQEDEQH/xAAfAAABBQEBAQEBAQAAAAAAAAAAAQIDBAUGBwgJCgv/xAC1EAACAQMDAgQDBQUEBAAAAX0BAgMABBEFEiExQQYTUWEHInEUMoGRoQgjQrHBFVLR8CQzYnKCCQoWFxgZGiUmJygpKjQ1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4eLj5OXm5+jp6vHy8/T19vf4+fr/xAAfAQADAQEBAQEBAQEBAAAAAAAAAQIDBAUGBwgJCgv/xAC1EQACAQIEBAMEBwUEBAABAncAAQIDEQQFITEGEkFRB2FxEyIygQgUQpGhscEJIzNS8BVictEKFiQ04SXxFxgZGiYnKCkqNTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqCg4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2dri4+Tl5ufo6ery8/T19vf4+fr/2gAMAwEAAhEDEQA/AP34ooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKTeMe2Mk+lAC0Um7ilz/PFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUYo3c0AFFUNY8Uad4fVTe3tvbeYQFDuAz54GB1P4VbtbyO9hEkLrIh6Fec0ANn1CG1dVkkSNpDhQxwSfp+f5VzPxO+I3/CD2cCwxLc3t1kQIThRjHzn8xXmvxomksfig1xjctvHCQpHYgD16/4/Wtbx5p134p0HR9YsYvtTWqGOaLq/JBBA6+o/KgCSBPHWu2jahHeGNdu8RLFtRl67cdefWuy+GXifUtf08rqVrJb3CcFiveuNi+M+tRWkNvb6DIrxjGXZlY7R6YqbQ/jRqGnarHDrGlx2y3DYEig4P0yaAPVs9P8AOKKjtbpb63WRPuyDIqSgAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKC2OvGBk+1Q3+oQ6ZZyXFxIsMMSlndj8qgUAS7hj69Pelz/8AX9q828T/ALQtraxSLpNnNf8Al8NO6lYf8T+lZ3hJ/EnxLv476fUNlvC4YRRfJGARkKe5/GgDsPFfxg0TwrK0DTfbLrn9xbgMwx1yeg/E1yq+OPFXja7jXT7VdLts5AxvkZeuCfpWN8RdEXwJ8R4buRd1rfjzGI43MPv54710F78cI2g+z+H9La4cjmaUFYgcdfVvxxQBn/H3wpcTaHYapNuaaE+TMQc4B+59cHPPHWq/wl+IM3hm6i02/cm3mAMEh6FT05/Cuv0Ky1bxp4UvLbWlT/SoyFIXAXuv5GvP/B2hJ4jtr3Qbr9zfWbN5DDqjLu/8d9qANj4p2EOrfElI85jvrJQG/wCBFRj8MGl+DniabQU1DTZo2mltwwESn75TPQ++K5yK9vrfxxZWmoLIt1YgwkkYyvUED0rutY+HN8nxBh1jT/L8mZA0uDhw/GSB07CgBmg/HPRiGXULX+z7pDgxeXuIwccd+awfFniVvi54isrfT7OSO0s23CVlw7E9wPbjr6V6dqXgbTdZPmXNnC8n8R/vH649au6XoFnoyAW9vHH7gfNQA7RLA6ZpNvbsctEuDVqiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKM8/rQAUbv54ozWPe+O9JsdX/s+W/t1vOhj3ZZeM8+hoA2O9NMqgN8y/Lyeeneob+/jtdLmuNwZUiaQEH0XNeV/B8X3iWLVXkuJpJLhHbfI2WLEMo9gKANLxf8ZpLy7ksdAtpZpITta4DLsVv9nIOf0qnoHxJvZb/+yfEFrN/pShQXAZJM9eAornfBniZvhff3EN9pc9xcR/KCqDAALZYeua2r341zalKszeGt0cY3ZY/Moz1ztoA7qXwBYS6BdQw28ObiJlBCEAZXjj61w/wG1dtH1i80ueRlaNim09NwOM/o1d34B8fWvjjTvMhXY6jmM9q8/wDidp7eB/iZa6pCPLhv2yxHTeCM/ngfkfWgDqvj54dbWvBTzRpvmsXEuMc7R1xVP4J3OnyeF/NcQrJEMvI+AAD0zXbWssXiHQlZl3RXUWGBHqOa8ws/2e7tL+SGS822Hmb1jBPygtkL7gDigDqNd+OGj6XdCC3kk1CYHBWFcgVkzeAr3UPiDHrtliOzuNrsg4bdxnJ/AV0nhr4U6V4cj+WESsOSXGQf610iL5SbVVVVeFAoAo3nhyz1C8juJLeJp15LsuWI/u5rQHA/QY4oooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAo6ij7u5m4FeZ+NPjFeXOotY+G1S4kjbDzModT/ALp3c/lQB6ZXJ/Eb4nR+BI4oY4vtd/c5aOEsQqgcFidtcdpnxd1/w9fRrr8Tx28j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KKACiiigAprNg06mv1oAN9G+m0UAO306o6XzfpQBLspGGDS76aTmgAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACmv0p1NfpQA2iiigAp6/KOtMooAc5+am0E5NFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFPXkdKAGUU6Sm0AFFFFABRRTvLoAE6U6hRtFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABTX606mv1oAbRRRQAVHUlFADvMpynIqOnJ0oAdRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFNfpTi2KazZFADaKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigApydKbTk6UAElNp0lNoAKKKKACplXctR7KkVtq0AIw2mkpWOTSUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFNfrTqay5NADaKCNtFABRRRQAU5OlGylUYFAC0UUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFADZKbTpKbQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABTk6U2nqMCgBJKbTpKbQAUUUUASUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAEZpuypEHFOxQBDso2VNijFAEdFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUANkptOkptABRRRQAUUUUAFFFFABRRRQBHuqSo6koAKKKKACiiigAooooAKcrYptFAATk0UUUAFFFFAElFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAAODRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAY4pNwp//LOoaAHOc02iigAooooAKKKKACiiigAooooAjp+4UyigCQHNFIn3aWgAooooAKKKKACiiigAooooAAM0uw0qfep1ABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRn9envQAUUZ/wooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigB3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fM9cc1JR3oAM0Y5oooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACmv1p1NfrQA2iiigAooooAKKKKACmyU6myUAIoyafTE+9Ts4GeMDk89B9KAFqSs+XXrO3uPKku7dZMf6suN386574t+ONQ8E6Fb3mnxQzJJIUkZ1J28ZGMevI/CgDr361i2vj3TbvXptNjm3XVudrgqy4J6Dp1rB8B/Gmz8V7Ybhltrtv4G/i+lc78WrE+FPH1jrUP8AqbwgOf8AbXpn9OfegB3j/wAZal4p8aSaDp91JZ28DCNjHJiSRyM5yOdoFF58D7/RLKO607UXW+jOSJJOD6/lVzxF8NpvFV1Hrmj332W6uI/nAYEE4wSMHPI7VRX4ReKNQb/SNYuicY4fA/8AQqAPSvCf2xdHiW+2+fjDY9awvjd4V/4SXwXOyD99Zgyx468fe/MVx/g/WtU8C+PG0S8upL1PkbLHJAbbjr65/SvXZIlljZGG5WGCD3HSgDx34JeD9L8R2kk1wFkmXBZQ2cjuW+hqP4wabpujeK9K/slo1vASZVjOSoJXBbH0NSN8I/EGm+IL6HT7lrezuJCVKNhihOTk9K6rwf8ABO10Sdbm6ke4uDgsT3I9zzQBk+P/AANfeIotF1Szj3XUMYWYfwsoxggde7f5ArvNNsBfaJDHeRq7qmCrjPNaLIEUKBgDpjtSUAZtn4Q020ufMjtYd2Sfu1qhAqgAYC8DFIn3806gAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKa/WnU1+tADaKKKACiiigAooooIuwpslOpslAXY0HBrh/jD8Rbrwwtvp+n7I72+UsZTj92oODxjrXcVx/xV+GsnjRLe4tJvs95aH5GYfKy5zjrQOJy9p8Ida1qw+3SateC9Zd65mPXGcVrQ+HtY8TfD2803Vh5l0iF4G3HJZenJ7nnP1rIHgrxtdhYZNTkWNehU8dOtVri58SfC7VrH7XfSXlveNsYM+/5uPX69aCjG8FeDn8TWzPYTNaavZkheeHIOCM+tWfF3jC61Xw/JpOtQPBqFkfMikPRjuxjPrjHTPStBJ28E/FJJY1ZLbVVW4RcY64J4z9f0r0rxV4H07x3pgW4hRsj5HH3lPqD2/CgDhfCfi3VtS+HX/EmMP2612naYwxKjhgO2c1Ivx11i1tvs8miSfbOm5ztXP4gVufDj4USeBL+ZvtXmwtnAI24zXYGwhMu/wAtQ3sKAPNPAPg3VPEPiltb1bIlkYMcjGAOij6Y4r1Wo9uBjtjp/WpKAG+Wv9Ce5/Gnf40UUABXdUKvliKmqFf9Y1AEidadTU606gAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKa/WnU1+tADaKKKACiiigAooooMwoK7qKKAGMMGqurQXFxps6Wsq29wyHy5sA7T2OOlXG+7TNuP0/SgqJ5XB488XeD/MtrzT/t0iNhZi2AR68Co7DwrrvxK1+O/1geXDDwkY6RDjoMe1erSQrKctljjHJ7VKi7FVQxAUYGKCjOuPCljdx26SW8cn2biMsoJA44z+A/IVoqiogXHyqMAUtFAB1A9vWiihRk0AGKkoooAKKKKAEZsVGFwc0+Sm0AOTrTqanWnUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABTZBjFOoYbqAI6KVl20lABRRRQK6CiiinysgKKKKQ+ViP92mU9/u0ygsKkpvl06gAooooAKVTg0KNxpwXBoAWiim+ZQA6im+ZTlORQA2Sm06Sm0AOTrTqjU4NSqNy5oASiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAbJTadJTaACiiigzCiiitACiiipZoDDIpuynAZp+wVIDKKKKACiiigBydadTUPzU6gAqOpKjoAKcnSm09RgUAJJTadJTaACpk+7ioamj5oAbRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUANkptOkptABRRRQZhRRRWgBRRRUyNBydadTU606pAMVHUlR0AFFFFAApwaN1FFABRRRQA/YKWiigBslNp0lNoAKFO3vRRQBJRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUANcZppXFSU1+lADaKKKCeUXYaNhp9FVzByjNhpCMVJTZKVyhFODTtwplFICSmbDT6KAGbDSEYqSmv1oAbRRRQAUuw0lSUAFBOKKa/WgAc5ptFFABRRRQBJRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFNfpTqa/SgBtFFFAElFN30b6AHU1xmjfQzUANooooAkopu+nUAFNk606kZd1ADKKdso2UANqSm7KdQAUx+tPpuygBtFOZdtNoAKKKKAJKKbvp1ABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAU1+lOpr9KAG0UUUAFFFFABRRRQAUUUUAFSVHUlABRRRQAUUUUAFFFFABRRRQA2Sm06Sm0AFFFFABUlR1JQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQBHRRRQAUUUUAFFFFAElFFFABRRRQAUUUUAFFFFABRRRQA2Sm06Sm0AFFFFABUlR1JQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQBHRRRQAUUUUAFFFFADvMp1R1JQAUUUUAFFFFABRRRQAUUUUANZd1Hl06igBvl0eXTqKAG+XTqKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAI6KKKACiiigAooooAKkqOjdQBJRUe6jdQBJRUe6nK3zUAOooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAI6KXYaSgAooooAKKKKACiiigAooooAKVOTSUA4oAkopNwpaACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACig/eqvqOpw6RYyXFzIsMMal2dzgAe9AFiivPb/8AaDsUmb7HY315bryZhHhT9B1xXSeC/iBY+N7YvasyspwUYfMPXP40Ab1FFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUxhg0+mv1oAbRRRQAUUUUAFFFFABRRRQAUUUUAFSVHUlABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRQOfrQAUVHcXS2kJkkZY1X7xdtqr9TXH+I/jjpGkM8Vn5mqXC9RB/q1+r/d/LNAHZM/ljLMuPXoPz7/pSq3mRhlIYeoNeafFrxVqsvgi11DT5prKFmAnVB83K5HPbnPT0qP4P/FeS/YafqUjeb/DI38YPQigCf42fEnVPB2qWVvZeUsciiRz5e5jg4I54x79ayvinr03i34cafdp5jW6zK9yE6H5dwz/ALI5FWPj/p66h4i0ld3yzRSRsf7o4IP61X+Dt1BqFjd6FqarJEm5CkvG3GR/LP5UASeFfiR4V8O6JHHJDJLcYAZBAW6++Mce1WPBfxB8K2WuyfY7eaxe4Y/NJHhCTz65q1pvwr8J6rPI1ncRypGzD93NvHBx97OP1rB+LvhDRfC9raR6eyrqjTKEjRxvII5JXOevtQB7JbzrdQLJG25XGQRTqxPh7LNL4XtWnzu2459K26ACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAG76dUdSUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFNfrTqa/WgBtFFFABRRRQAUUUUAKq7qRhg09TkUj/eoAbRRRQAU7fTaKAJKKKKACiiigAooooAKKKKACiimq+foDgkdj6UAOpobn+Hv0OT+WK5bxj8XtN8I3RtSs15eAZMUK7tn+8egrK0n442evTmxu7e8017hSEZjx/wB9UAdV4j8b6X4Tg3395Db7uQpbLt34Xr+dcZdfHWfWb1ItE0ySaJmCmac7c84ICjnnsTin2nwFspjNPNczXTXCOBJI252yMZOTzWL8D7xfD/iW80q5VVmhdogxGCcH9cHpnsRQBn/Fb7dc+MLVdQmuPsV0gZIcnbGw4YbR159a7Kx0rwr8P9Ojubqe1STbuTJ+fpn5VHNTfHbwv/bng5rmJd0+nnzUI6qP4sY59/rXI/CH4fWHi63N1cs0sy43ByGZ+x69BQB2Fv4l034qaFfaba280cUiMkZlj2biBkED0zjrjpXl/hvQZtXSazjZodW093MTdN2P4f0NezTXuh/D622yTW9rvABUnMknpkdfzrg/EmiXFp8SYdX0uFp7O+AkldOisOOR7n+dAGHrXjOTxEdHjuo2jvLCcwOnf7owAfw6n1FaXiOxbwl4+sNQhjJg1SFGYKCQGAw34/8Axddd4o+Dlp4p1eDVFY2tztBlAGFkIGOR+X5V1kOkQiyghkUSLD93dzigDgdS+CLXNz9u0m/u7Frj5yiuVwSc8c1a8MfAyHTLsTX1zLdSDsWLE85713yxqgwAoA6DFOHFADYYVt4lRF2qowAO1OoooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAjqSk2CloAKKKKACiiigAooooAKKKKACiiigAooooAKa/WnUEZoAjop+wUbBQAyinPTaACiiigB6jApJBg0itg0q/N1oAbRT9go2CgBlFP2CjYKAFooooAKKKKACijGTWb4n8WWPg/TftV/MIYu3BLP7AevSgDSHJ6U0yhSA3y7ume/0rzeX9oqMSGRNHvPsef9acAj1yvbH1qLxLpUnxHhTWtB1C4juoV3eQZDtx6AZ4PtQB6cWAXOR781534D+JGoeLvFV+jLD5cO9IlReQFOOfXNZ+jfGWay0680/Wo3tdQj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HT9K39R/aF0e5g8mGxuriM5GNi4B7dfSrtt450v/AIV9DealC1wkWI8JFvYFuc8nA/Gr1jrfg9NON0J7NIyu4hpeT+Gc0AP+HnxR0rxPm2t4XtHjI/dyDHB/Sul17TF1rRLq1KqwuImjGenI/wAa8h0Vo/FnxV+3aXC0NmpVASNpYjblsdOcfrXtKDCigDw34a/ECP4bz3tlerNJJCxURJ95iDxx9Afzqe8TVvi34rt7iS3kjsrcgwpJ0XnJPAA7V6hcfDzSbnUzeSWkbTM29jjq3rmti2tYrNNscYRR0CjAoAop4bt59Jt7W6jjmEK45XIJ9RWhbwR2luscahVXjAp1FAAeaKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiimbjQAOcmljptNZ9tAE1FGc0UAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFeX/H/RbwXum6hHDLNa2+Y5BGPulmznofpXqFVdVu4NPsJZ7raLeNS0h2luB7UAeXWfx2trLTY7a10O6cqu0hgORjpU/hT4t6fLdGxutJbTxdrs2k7kYN2OQCevUV1Xh3xD4b8QwNcW5tlVeu8CPH51wvxb1mx8T+JtNs9J2XDWkhaaRF6EYwoP9KAE8AOvg74gXmkzjzLWViYyVzuRsMM9uhX8jWt8QPgfvSa70Nvs8ki4eA8RyDOcVZ8SfDK61O80e+t1RZ7aLy5fmxkBgeePQtXoVorRWsat95RzmgDyf4MaLebLrS9Ts3+zSAqVdfvDqAfp0ropPgDopufMUSLznaGJH0613GwKflX7x5NOoAztA8LWPhqEJawqnHXHP0rRoooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKjqQnFR0AFNkp1NcZoAkTpTqah4p1ABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABTZIlmjZXQMrDBzznPXinUUAcDrH7P2j6hePNDvtfMbcVH3c/Qdq2PC3wt0zwkytDH5kqj7xUcf4mumooAQjBHHy0tFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAMm+7SUs33aSgAooooAcnWnU1OtOoAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigCO46UUUUAFFFFADk606iigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAP/2QplbmRzdHJlYW0KZW5kb2JqCjUgMCBvYmoKPDwvRmlsdGVyL0ZsYXRlRGVjb2RlL0xlbmd0aCAxMzQyPj5zdHJlYW0KeJy9WNtu2zgQfddXDLAvKQrLJEVS5L45qVN40zhprAVSbPbBdZRUC9tqJblp+yf7tzvUzZdWtGRngwTRQBnyzJwZDmf0xTkNHE+CIhKCe2cYOO8dBn+YtxQI/pi/ijMIFk7/nAIlEDw4J6+Cf4zuFxBC5GqKMGDE/M4W0I8WjwTexPB+c5/i37hPsQbRerU0NGpPDkFkkqvfvHX++huf9w7VrgDFGOBCX5Ty3Jk0LkAQVEHFhRHz5Tb1XAEVq+1RtKmjUr29kev9K3Rcs2bO0LHJAWUafE+4zK8J5YbQAiV5dE6ooZbAY0HJ1j6tEJjcA3C2SrN4ESYwevP7M2BRacG6/hQvQxivFh/D5FiwnDpEk6pG87bRXlNMQ9JjSvW07/HjnZNaun6jc+fTbx1da8xXX1BMJ48QzCUj70lvqdfqRrapo5bZMs9XI0tNOiUsUqAV+Np3Dd2GCQZUbTMxGU/gdHQ1+TAJhpcTGI3P3GPJR984+D51laz5FzuoWZhE8+gHpvJlvIyyOImWj3CdxI/JdHEkvkfMMfJduVP41uiD+0W0hMFsFq+W2XHx17oMu6kofG8CNEfUjlBkjUGocmbDOOJqTjGu+PQZVmVqnCxTjeGzh9FPQufBwhmmiVTEVaxKE73N2EWUwWj5ECeLaRbFy2bOrJW68ILxNrlf2F/dBNL321wF0ldrAJStAHwDgW9CbFErmvH8TY/Y2qXmRaVTbMMrX+xZUwLVnrG1axYgvonEt6BaHSIfgRhIiUW0Lh07leNiFMBv3TMh91uVBEjaKqzSdBC0CKu0dxCUV/vnUg3QrphvrCw5bldpfWLnanh7DW8GwfCAg5N7wdZpvcd/VvvPuvpPt5d2IUByV2uQWNlUXXV3SgilxPMPS5fCfRS43h/7Kldq7XYRNB4ouweMUK9PWd+z9HnWsFRh7GaabxpW6SmLYewwXtGKglccIvbymivnUqndiVer+YSKPtF9jxzIa+FHV9PKnGWi2TLOlT4k2IUlBbfM289trpxLpXYnbm0eILe6T2mfHJqzhR9dTSu5pazZMixNytLn27hFSwpuqX3eqxgtuS21O3Fr86DIW3543hZ+/Nq0X4s2Vkh9CshP1aVl4wyC+i6urvpmtu1w8CnMZ4XheHJ1A//Cxyiex4/RbDqH9HOchJCFaYatZrqaZylMH7DJBx/up99TiB8gWs5WH/P2EaZJeOxIyXFyFRJbtvVISbetvZl9ve9BYEy6CWdh9DW8fw5MziyYeM9flJimZR5++xwlLWHbzVZcukSBUArvhGqi27EhGE4CuBlO/nwXwMXwwyG9OgKIct4wUutTwwQuwNaxmaDzwehdD94m8VP2ySRFnjF58qTHjp0GW3AL9lm8zJJ4XqHfnby+e/UcoJ5uATqO17i9triW/p6XMcolr7xjGvU5FphCP5e8DqXco/tCGiUYw/NpNIcePuZpCNdxGmV44PDgmQAfO9DTPYG9HkwmvQ2GnzWvDLo1wsFTDCbK0XIVr9KciBQd/zyNElh/5+heAxoPpyT1xwBiH6LNSfa86iT/HPV2BODkyIBr4bIDR8FWMEq6yKwVxkxQcHUOpsId+0VSEjtWUUCfsXIz9iI0MjyrL8ijh+XnRXnkRL8Ij1zol+RRMP3/8NjYL/Lyk6LIpdbXATYgkmMf0twW73xRhmv3yoXTq1u8RpQGgMlgHAzgcnAzGsDZALTHhcTXdyeKiLtXoJnoUUnNq/PB7dZrYiwFSJcpdp7p9zQLF6k7iy0fjC3+e37pPgo73v8HL9GdGgplbmRzdHJlYW0KZW5kb2JqCjEgMCBvYmoKPDwvVGFicy9TL0dyb3VwPDwvUy9UcmFuc3BhcmVuY3kvVHlwZS9Hcm91cC9DUy9EZXZpY2VSR0I+Pi9Db250ZW50cyA1IDAgUi9UeXBlL1BhZ2UvUmVzb3VyY2VzPDwvQ29sb3JTcGFjZTw8L0NTL0RldmljZVJHQj4+L1Byb2NTZXQgWy9QREYgL1RleHQgL0ltYWdlQiAvSW1hZ2VDIC9JbWFnZUldL0ZvbnQ8PC9GMSAyIDAgUi9GMiA0IDAgUj4+L1hPYmplY3Q8PC9pbWcwIDMgMCBSPj4+Pi9QYXJlbnQgNiAwIFIvTWVkaWFCb3hbMCAwIDU5NSA4NDJdPj4KZW5kb2JqCjcgMCBvYmoKWzEgMCBSL1hZWiAwIDg1MiAwXQplbmRvYmoKMiAwIG9iago8PC9TdWJ0eXBlL1R5cGUxL1R5cGUvRm9udC9CYXNlRm9udC9IZWx2ZXRpY2EvRW5jb2RpbmcvV2luQW5zaUVuY29kaW5nPj4KZW5kb2JqCjQgMCBvYmoKPDwvU3VidHlwZS9UeXBlMS9UeXBlL0ZvbnQvQmFzZUZvbnQvSGVsdmV0aWNhLUJvbGQvRW5jb2RpbmcvV2luQW5zaUVuY29kaW5nPj4KZW5kb2JqCjYgMCBvYmoKPDwvS2lkc1sxIDAgUl0vVHlwZS9QYWdlcy9Db3VudCAxL0lUWFQoMi4xLjcpPj4KZW5kb2JqCjggMCBvYmoKPDwvTmFtZXNbKEpSX1BBR0VfQU5DSE9SXzBfMSkgNyAwIFJdPj4KZW5kb2JqCjkgMCBvYmoKPDwvRGVzdHMgOCAwIFI+PgplbmRvYmoKMTAgMCBvYmoKPDwvTmFtZXMgOSAwIFIvVHlwZS9DYXRhbG9nL1BhZ2VzIDYgMCBSL1ZpZXdlclByZWZlcmVuY2VzPDwvUHJpbnRTY2FsaW5nL0FwcERlZmF1bHQ+Pj4+CmVuZG9iagoxMSAwIG9iago8PC9Nb2REYXRlKEQ6MjAyMzAxMzAwMDQxMDQrMDUnMzAnKS9DcmVhdG9yKEphc3BlclJlcG9ydHMgTGlicmFyeSB2ZXJzaW9uIDYuMjAuMC0yYmM3YWI2MWM1NmY0NTllODE3NmViMDVjNzcwNWUxNDVjZDQwMGFkKS9DcmVhdGlvbkRhdGUoRDoyMDIzMDEzMDAwNDEwNCswNSczMCcpL1Byb2R1Y2VyKGlUZXh0IDIuMS43IGJ5IDFUM1hUKT4+CmVuZG9iagp4cmVmCjAgMTIKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDE4ODkyIDAwMDAwIG4gCjAwMDAwMTkyMDAgMDAwMDAgbiAKMDAwMDAwMDAxNSAwMDAwMCBuIAowMDAwMDE5Mjg4IDAwMDAwIG4gCjAwMDAwMTc0ODIgMDAwMDAgbiAKMDAwMDAxOTM4MSAwMDAwMCBuIAowMDAwMDE5MTY1IDAwMDAwIG4gCjAwMDAwMTk0NDQgMDAwMDAgbiAKMDAwMDAxOTQ5OCAwMDAwMCBuIAowMDAwMDE5NTMwIDAwMDAwIG4gCjAwMDAwMTk2MzQgMDAwMDAgbiAKdHJhaWxlcgo8PC9JbmZvIDExIDAgUi9JRCBbPDgyMDRmZTcwMGYzODFiYjY1YjM0YzBlODljZjFhYWUyPjw4ODIxOTk0NTZjMTcyODcwYzNjZTRiYzg4N2I2OTBmND5dL1Jvb3QgMTAgMCBSL1NpemUgMTI+PgpzdGFydHhyZWYKMTk4NDQKJSVFT0YK";

    @Override
    public void start(Stage stage) throws IOException {

        primaryStage = stage;
        //    Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("testPagination.fxml")));
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("auth/login.fxml")));
        //    stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(AppConfig.APPLICATION_ICON))));
        stage.setTitle(AppConfig.APPLICATION_NAME);
        stage.setMaximized(true);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public void changeScene(String fxml, String title) {

        try {
            if (null != primaryStage && primaryStage.isShowing()) {
                Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
                primaryStage.getScene().setRoot(pane);
                Platform.runLater(() -> {
                    primaryStage.setTitle(AppConfig.APPLICATION_NAME + " ( " + title + " ) ");
                });
                primaryStage.show();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ParseException, IOException {
        launch(args);
    }

}