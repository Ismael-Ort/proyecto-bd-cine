package javaDB;

import logico.Funcion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FuncionBD {

    public boolean registrarFuncion(Funcion funcion) {

        String sql = "INSERT INTO funcion (fecha_funcion, hora_inicio, hora_fin, tarifa_base, estado, id_pelicula, id_sala, idioma_audio, idioma_subtitulos) VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(funcion.getFechaFuncion()));
            ps.setTime(2, Time.valueOf(funcion.getHoraInicio()));
            ps.setTime(3, Time.valueOf(funcion.getHoraFin()));
            ps.setBigDecimal(4, funcion.getTarifaBase());
            ps.setString(5, funcion.getEstado());
            ps.setInt(6, funcion.getIdPelicula());
            ps.setInt(7, funcion.getIdSala());
            ps.setString(8, funcion.getIdiomaAudio());
            ps.setString(9, funcion.getIdiomaSubtitulos());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al registrar funcion: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo registrar la funcion" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la funcion" + e.getMessage(), e);
        }
    }

    public boolean actualizarFuncion(Funcion funcion) {

        String sql = "UPDATE funcion SET fecha_funcion = ?, hora_inicio = ?, hora_fin = ?, tarifa_base = ?, estado = ?, id_pelicula = ?, id_sala = ?, idioma_audio = ?, idioma_subtitulos = ? WHERE id_funcion = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(funcion.getFechaFuncion()));
            ps.setTime(2, Time.valueOf(funcion.getHoraInicio()));
            ps.setTime(3, Time.valueOf(funcion.getHoraFin()));
            ps.setBigDecimal(4, funcion.getTarifaBase());
            ps.setString(5, funcion.getEstado());
            ps.setInt(6, funcion.getIdPelicula());
            ps.setInt(7, funcion.getIdSala());
            ps.setString(8, funcion.getIdiomaAudio());
            ps.setString(9, funcion.getIdiomaSubtitulos());
            ps.setInt(10, funcion.getIdFuncion());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar funcion: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar la funcion" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la funcion" + e.getMessage(), e);
        }
    }

    public List<Funcion> listarFunciones() {

        String sql = "SELECT id_funcion, fecha_funcion, hora_inicio, hora_fin, tarifa_base, estado, id_pelicula, id_sala, idioma_audio, idioma_subtitulos " +
                "FROM funcion ORDER BY fecha_funcion, hora_inicio";

        List<Funcion> funciones = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                funciones.add(mapearFuncion(rs));
            }

        } catch (SQLException e) {

            System.out.println("Error al listar funciones: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las funciones" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las funciones" + e.getMessage(), e);
        }

        return funciones;
    }

    // Funciones PROGRAMADA/EN_CURSO ordenadas por fecha/hora, para el panel
    // de control ("Proximas funciones").
    public List<Funcion> listarProximasFunciones(int maximo) {

        String sql = "SELECT id_funcion, fecha_funcion, hora_inicio, hora_fin, tarifa_base, estado, id_pelicula, id_sala, idioma_audio, idioma_subtitulos " +
                "FROM funcion WHERE estado IN ('PROGRAMADA','EN_CURSO') ORDER BY fecha_funcion, hora_inicio LIMIT ?";

        List<Funcion> funciones = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, maximo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    funciones.add(mapearFuncion(rs));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al listar proximas funciones: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las funciones" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las funciones" + e.getMessage(), e);
        }

        return funciones;
    }

    // No se puede expresar con un CHECK de una sola fila: hay que comparar
    // contra las demas funciones de la misma sala y fecha. idFuncionExcluir
    // sirve para no chocar consigo misma cuando se esta editando (usar 0
    // para una funcion nueva).
    public boolean existeChoqueHorario(int idSala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, int idFuncionExcluir) {

        String sql = "SELECT COUNT(*) AS total FROM funcion " +
                "WHERE id_sala = ? AND fecha_funcion = ? AND estado NOT IN ('CANCELADA','FINALIZADA') " +
                "AND id_funcion <> ? AND hora_inicio < ? AND hora_fin > ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSala);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setInt(3, idFuncionExcluir);
            ps.setTime(4, Time.valueOf(horaFin));
            ps.setTime(5, Time.valueOf(horaInicio));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }

            return false;

        } catch (SQLException e) {

            System.out.println("Error al validar choque de horario: " + e.getMessage());
            throw new RuntimeException("No se pudo validar el horario" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la funcion" + e.getMessage(), e);
        }
    }

    private Funcion mapearFuncion(ResultSet rs) throws SQLException {

        Funcion funcion = new Funcion();

        funcion.setIdFuncion(rs.getInt("id_funcion"));
        funcion.setFechaFuncion(rs.getDate("fecha_funcion").toLocalDate());
        funcion.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        funcion.setHoraFin(rs.getTime("hora_fin").toLocalTime());
        funcion.setTarifaBase(rs.getBigDecimal("tarifa_base"));
        funcion.setEstado(rs.getString("estado"));
        funcion.setIdPelicula(rs.getInt("id_pelicula"));
        funcion.setIdSala(rs.getInt("id_sala"));
        funcion.setIdiomaAudio(rs.getString("idioma_audio"));
        funcion.setIdiomaSubtitulos(rs.getString("idioma_subtitulos"));

        return funcion;
    }
}
