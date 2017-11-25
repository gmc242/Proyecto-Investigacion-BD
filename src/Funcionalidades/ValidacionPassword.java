package Funcionalidades;


import java.util.Arrays;
import javafx.util.Pair;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class ValidacionPassword {

    public static Pair<byte[], byte[]> generarEncriptado(String password) throws Exception{
        try {
            byte[] sal = generarSal();
            byte[] encriptado = generarHash(password, sal, 1000);
            return new Pair<>(sal, encriptado);
        }catch (Exception e){
            throw new Exception("No se pudo generar un encriptado para el password ingresado\n" +
                    "Razon: " + e.getMessage());
        }
    }

    public static boolean esPasswordValido(byte[] encriptado, byte[] sal, String candidato) throws Exception{
        try{
            byte[] encriptadoCandidato = generarHash(candidato, sal, 1000);
            if(Arrays.equals(encriptado, encriptadoCandidato))
                return true;
            return false;
        }catch (Exception e){
            throw new Exception("No se pudo generar un encriptado para el password ingresado\n" +
                    "Razon: " + e.getMessage());
        }
    }

    private static byte[] generarHash(String password, byte[] sal, int iteraciones) throws Exception{
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), sal, iteraciones, 64 * 8);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return keyFactory.generateSecret(spec).getEncoded();
    }

    private static byte[] generarSal() throws Exception{
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] sal = new byte[16];
        random.nextBytes(sal);
        return sal;
    }
}
